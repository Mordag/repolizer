package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.RepositoryParameter
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.annotation.repository.util.ParameterType
import repolizer.repository.RepositoryMapHolder
import repolizer.repository.RepositoryProcessorUtil.Companion.buildSql
import repolizer.repository.RepositoryProcessorUtil.Companion.buildUrl
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

class RepositoryGetMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkFutureBuilder")
    private val classRequestType = ClassName.get("repolizer.repository.request", "RequestType")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")

    fun build(element: Element): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.getAnnotationMap[element.simpleName.toString()]?.map { methodElement ->
                MethodSpec.methodBuilder(methodElement.simpleName.toString()).apply {
                    addModifiers(Modifier.PUBLIC)
                    addAnnotation(Override::class.java)
                    returns(ClassName.get(methodElement.returnType))

                    //Copy all interface parameter to the method implementation
                    methodElement.parameters.forEach { varElement ->
                        val varType = ClassName.get(varElement.asType())
                        addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
                    }

                    val annotationMapKey = "${element.simpleName}.${methodElement.simpleName}"

                    //Generates the code which used to retrieve the url from the annotation
                    //and dynamic parameter with method parameter (like the url part
                    //':myVar' could be the value '0'
                    val url = methodElement.getAnnotation(GET::class.java).url
                    addStatement("String url = \"$url\"")
                    addCode(buildUrl(annotationMapKey))

                    val insertSql = methodElement.getAnnotation(GET::class.java).insertSql
                    addStatement("String insertSql = \"$insertSql\"")
                    if (insertSql.isNotEmpty()) addCode(buildSql(annotationMapKey,
                            "insertSql", insertSql))

                    val querySql = methodElement.getAnnotation(GET::class.java).querySql
                    addStatement("String querySql = \"$querySql\"")
                    if (querySql.isNotEmpty()) addCode(buildSql(annotationMapKey,
                            "querySql", querySql))

                    val deleteSql = methodElement.getAnnotation(GET::class.java).deleteSql
                    addStatement("String deleteSql = \"$deleteSql\"")
                    if (deleteSql.isNotEmpty()) addCode(buildSql(annotationMapKey,
                            "deleteSql", deleteSql))

                    val classWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                            ClassName.get(methodElement.returnType))
                    addStatement("$classTypeToken returnType = new $classWithTypeToken() {}")

                    addCode("\n")

                    //Generates the code which will be used for the NetworkBuilder to
                    //initialise it's values
                    addCode(getBuilderCode(annotationMapKey, element, methodElement))

                    //Generates the code which is defined the @RepositoryParameter annotation.
                    //Those values are used to tweak the behavior of the repository regarding certain
                    //cases (like cannot refresh data due to error and cache is too old).
                    addCode(getRepositoryCode(element, annotationMapKey))

                    addStatement("return super.executeGet(builder, returnType.getType())")
                }.build()
            } ?: ArrayList())
        }
    }

    private fun getBuilderCode(annotationMapKey: String, classElement: Element,
                               methodElement: ExecutableElement): String {

        return ArrayList<String>().apply {
            val annotation = methodElement.getAnnotation(GET::class.java)

            add("$classNetworkBuilder builder = new $classNetworkBuilder();")

            add("builder.setRequestType($classRequestType.GET);")
            add("builder.setTypeToken(returnType);")
            add("builder.setUrl(url);")
            add("builder.setRequiresLogin(${annotation.requiresLogin});")
            add("builder.setShowProgress(${annotation.showProgress});")
            add("builder.setSaveData(${annotation.saveData});")
            add("builder.setRepositoryClass(${ClassName.get(classElement.asType())}.class);")
            add("builder.setFetchSecurityLayer(this);")

            add("builder.setInsertSql(insertSql);")
            add("builder.setQuerySql(querySql);")
            add("builder.setDeleteSql(deleteSql);")

            add("builder.setFreshCacheTime(${annotation.maxFreshTime}L);")
            add("builder.setMaxCacheTime(${annotation.maxCacheTime}L);")

            RepositoryMapHolder.requestBodyAnnotationMap[annotationMapKey]?.forEach {
                add("builder.setRaw(${it.simpleName});")
            }

            RepositoryMapHolder.headerAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addHeader(" +
                        "\"${it.getAnnotation(Header::class.java).key}\", ${it.simpleName});")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addQuery(" +
                        "\"${it.getAnnotation(UrlQuery::class.java).key}\", ${it.simpleName});")
            }

            RepositoryMapHolder.progressParamsAnnotationMap[annotationMapKey]?.forEach {
                add("builder.setProgressData(${it.simpleName});")
            }
        }.joinToString(separator = "\n", postfix = "\n")
    }

    private fun getRepositoryCode(element: Element, annotationMapKey: String): String {
        return ArrayList<String>().apply {
            var allowFetchParamName: String? = null
            var deleteIfCacheIsTooOldParamName: String? = null
            var allowMultipleRequestsSameTimeParamName: String? = null

            RepositoryMapHolder.repositoryParameterAnnotationMap[annotationMapKey]?.forEach { variable ->
                val type = variable.getAnnotation(RepositoryParameter::class.java).type
                when (type) {
                    ParameterType.ALLOW_FETCH -> allowFetchParamName = variable.simpleName.toString()
                    ParameterType.DELETE_IF_CACHE_TOO_OLD -> deleteIfCacheIsTooOldParamName = variable.simpleName.toString()
                    ParameterType.ALLOW_MULTIPLE_REQUESTS_SAME_TIME -> allowMultipleRequestsSameTimeParamName = variable.simpleName.toString()
                }
            }

            if (deleteIfCacheIsTooOldParamName != null) {
                add("builder.setDeletingCacheIfTooOld($deleteIfCacheIsTooOldParamName);")
            } else {
                val deleteIfCacheIsTooOldByDefault = element.getAnnotation(Repository::class.java).deleteIfCacheIsTooOld
                add("builder.setDeletingCacheIfTooOld($deleteIfCacheIsTooOldByDefault);")
            }

            if (allowFetchParamName != null) {
                add("builder.setAllowFetch($allowFetchParamName);")
            } else {
                val allowFetchByDefault = element.getAnnotation(Repository::class.java).allowFetchByDefault
                add("builder.setAllowFetch($allowFetchByDefault);")
            }

            if (allowMultipleRequestsSameTimeParamName != null) {
                add("builder.setAllowMultipleRequestsAtSameTime($allowMultipleRequestsSameTimeParamName);")
            } else {
                val deleteIfCacheIsTooOldByDefault = element.getAnnotation(Repository::class.java).allowMultipleRequestsAtSameTime
                add("builder.setAllowMultipleRequestsAtSameTime($deleteIfCacheIsTooOldByDefault);")
            }
        }.joinToString(separator = "\n", postfix = "\n")
    }
}