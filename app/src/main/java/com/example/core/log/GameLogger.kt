package com.example.core.log

/**
 * Standardized logging abstraction for Frontier Settlement.
 * Enforces structured logging while preventing sensitive information leak in release builds,
 * with safe fallback for JVM unit tests.
 */
object GameLogger {
    private const val TAG = "FrontierSettlement"

    var isDebugEnabled: Boolean = false
    var logHandler: ((priority: Int, tag: String, message: String, throwable: Throwable?) -> Unit)? = null

    fun d(tag: String = TAG, message: String) {
        if (isDebugEnabled) {
            log(3, tag, message, null)
        }
    }

    fun i(tag: String = TAG, message: String) {
        log(4, tag, message, null)
    }

    fun w(tag: String = TAG, message: String, throwable: Throwable? = null) {
        log(5, tag, message, throwable)
    }

    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) {
        log(6, tag, message, throwable)
    }

    private fun log(priority: Int, tag: String, message: String, throwable: Throwable?) {
        try {
            logHandler?.invoke(priority, tag, message, throwable) ?: run {
                when (priority) {
                    3 -> android.util.Log.d(tag, message)
                    4 -> android.util.Log.i(tag, message)
                    5 -> if (throwable != null) android.util.Log.w(tag, message, throwable) else android.util.Log.w(tag, message)
                    6 -> if (throwable != null) android.util.Log.e(tag, message, throwable) else android.util.Log.e(tag, message)
                }
            }
        } catch (_: Throwable) {
            // Safe fallback for JUnit JVM testing environment
            if (priority >= 5) {
                System.err.println("[$tag] $message")
                throwable?.printStackTrace()
            }
        }
    }
}
