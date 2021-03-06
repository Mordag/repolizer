package repolizer

import com.google.auto.service.AutoService
import repolizer.annotation.repository.*
import repolizer.annotation.repository.parameter.*
import repolizer.repository.RepositoryMainProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
class MainProcessor : AbstractProcessor() {

    lateinit var filer: Filer
        private set
    lateinit var messager: Messager
        private set
    lateinit var elements: Elements
        private set

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
        elements = processingEnvironment.elementUtils
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            //only in the first round has elements that can be processed
            if (set.isNotEmpty()) {
                RepositoryMainProcessor().process(this, roundEnv)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
                //General repository annotations
                Repository::class.java.name,

                //Repository method annotations
                REFRESH::class.java.name, GET::class.java.name, DATA::class.java.name,
                CUD::class.java.name, CACHE::class.java.name,

                //Repository parameter annotations
                DataBody::class.java.name, CacheBody::class.java.name, Header::class.java.name,
                RepositoryParameter::class.java.name, RequestBody::class.java.name,
                StatementParameter::class.java.name, UrlParameter::class.java.name,
                UrlQuery::class.java.name, MultipartBody::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}