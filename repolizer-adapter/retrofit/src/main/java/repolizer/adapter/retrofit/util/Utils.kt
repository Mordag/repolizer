package repolizer.adapter.retrofit.util

class Utils {

    companion object {

        fun prepareUrl(url: String): String {
            return url.split("?")[0]
        }
    }
}