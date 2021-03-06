package repolizer.repository

import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

object RepositoryMapHolder {

    val cacheAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val dbAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val cudAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val getAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val refreshAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()

    val dataBodyAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val cacheBodyAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val repositoryParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val headerAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val requestBodyAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val statementParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val urlParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val urlQueryAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val multipartBodyAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
}