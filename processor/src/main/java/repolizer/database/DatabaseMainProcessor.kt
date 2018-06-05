package repolizer.database

import com.squareup.javapoet.*
import repolizer.MainProcessor
import repolizer.annotation.database.Database
import repolizer.annotation.database.Migration
import repolizer.util.AnnotationProcessor
import repolizer.util.ProcessorUtil
import repolizer.util.ProcessorUtil.Companion.getGeneratedDatabaseName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class DatabaseMainProcessor : AnnotationProcessor {

    private val classCacheItem = ClassName.get("repolizer.database.cache", "CacheItem")
    private val classRepolizerDatabase = ClassName.get("repolizer.database", "RepolizerDatabase")

    private val classAnnotationDatabase = ClassName.get("android.arch.persistence.room", "Database")
    private val classAnnotationTypeConverters = ClassName.get("android.arch.persistence.room", "TypeConverters")

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        initMigrationAnnotations(mainProcessor, roundEnv)

        roundEnv.getElementsAnnotatedWith(Database::class.java).forEach {
            val typeElement = it as TypeElement

            if (!it.kind.isInterface) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                        "be applied to an interface. Error in class: ${typeElement.simpleName}")
                return
            }

            if(!typeElement.interfaces.isEmpty()) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Parent " +
                        "interfaces are not allowed. Error in class: ${typeElement.simpleName}")
                return
            }

            //Database annotation general data
            val databaseName = it.simpleName.toString()
            val databasePackageName = ProcessorUtil.getPackageName(mainProcessor, it)
            val databaseClassName = ClassName.get(databasePackageName, databaseName)

            val realDatabaseName = getGeneratedDatabaseName(databaseName)
            val realDatabaseClassName = ClassName.get(databasePackageName, realDatabaseName)

            val version = it.getAnnotation(Database::class.java).version
            val exportSchema = it.getAnnotation(Database::class.java).exportSchema

            val fileBuilder = TypeSpec.classBuilder(getGeneratedDatabaseName(databaseName))
                    .superclass(classRepolizerDatabase)
                    .addSuperinterface(databaseClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

            val entities = DatabaseMapHolder.entityMap[databaseName]
            val entitiesFormat: String = if (entities != null) addEntityClassesToDatabase(entities)
            else addEntityClassesToDatabase(ArrayList())

            val daoClasses: ArrayList<ClassName>? = DatabaseMapHolder.daoMap[databaseName]
            addDaoClassesToDatabase(daoClasses, fileBuilder)

            fileBuilder.addAnnotation(AnnotationSpec.builder(classAnnotationDatabase)
                    .addMember("entities", entitiesFormat)
                    .addMember("version", "$version")
                    .addMember("exportSchema", "$exportSchema")
                    .build())

            val converterFormat = addConvertersToDatabase(it)
            if (!converterFormat.isEmpty()) {
                fileBuilder.addAnnotation(AnnotationSpec.builder(classAnnotationTypeConverters)
                        .addMember("value", converterFormat)
                        .build())
            }

            val providerFile = DatabaseProvider().build(it, databaseName, realDatabaseClassName)
            JavaFile.builder(databasePackageName, providerFile)
                    .build()
                    .writeTo(mainProcessor.filer)

            val repoFile = fileBuilder.build()
            JavaFile.builder(databasePackageName, repoFile)
                    .build()
                    .writeTo(mainProcessor.filer)
        }
    }

    private fun addEntityClassesToDatabase(entities: ArrayList<ClassName>): String {
        var format = "{$classCacheItem.class"

        entities.forEach { entityName ->
            format += ", $entityName.class"
        }

        return "$format}"
    }

    private fun addDaoClassesToDatabase(daoList: ArrayList<ClassName>?, fileBuilder: TypeSpec.Builder) {
        daoList?.forEach { daoClassName ->
            fileBuilder.addMethod(createDatabaseDaoFunction(daoClassName))
        }
    }

    private fun createDatabaseDaoFunction(daoClassName: ClassName): MethodSpec {
        return MethodSpec.methodBuilder("get${daoClassName.simpleName()}")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(daoClassName)
                .build()
    }

    @Suppress("UNCHECKED_CAST")
    private fun addConvertersToDatabase(element: Element): String {
        var converterFormat = ""
        element.annotationMirrors.forEach {
            it.elementValues.forEach {
                val key = it.key.simpleName.toString()
                val value = it.value.value

                if (key == "value") {
                    val typeMirrors = value as List<AnnotationValue>
                    typeMirrors.forEach {
                        val declaredType = it.value as DeclaredType
                        val objectClass = declaredType.asElement()

                        if (!converterFormat.isEmpty()) {
                            converterFormat += ", "
                        }
                        converterFormat += "$objectClass.class"
                    }
                }
            }
        }
        return "{$converterFormat}"
    }

    private fun initMigrationAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        val hashMap: HashMap<String, ArrayList<Element>> = DatabaseMapHolder.migrationAnnotationMap

        roundEnv.getElementsAnnotatedWith(Migration::class.java).forEach {
            if (!it.kind.isInterface) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                        "be applied to an interface. Error in class: ${it.simpleName}")
                return
            }

            var currentList: ArrayList<Element>? = hashMap[it.simpleName.toString()]
            if (currentList == null) {
                currentList = ArrayList()
            }

            currentList.add(it)
            hashMap[it.simpleName.toString()] = currentList
        }
    }
}