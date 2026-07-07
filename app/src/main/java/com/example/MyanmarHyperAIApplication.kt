package com.example

import android.app.Application
import android.content.Context
import android.util.Log

class MyanmarHyperAIApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        bypassHiddenApiRestrictions()
    }

    private fun bypassHiddenApiRestrictions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            try {
                // 1. Get the standard getDeclaredMethod from Class class
                val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java,
                    java.lang.reflect.Array.newInstance(Class::class.java, 0).javaClass
                )
                
                // 2. Use double reflection to obtain Class.forName method
                val forName = getDeclaredMethod.invoke(
                    Class::class.java,
                    "forName",
                    arrayOf(String::class.java)
                ) as java.lang.reflect.Method
                
                // 3. Use double reflection to obtain Class.getDeclaredMethod method
                val getDeclaredMethodReflect = getDeclaredMethod.invoke(
                    Class::class.java,
                    "getDeclaredMethod",
                    arrayOf(
                        String::class.java,
                        java.lang.reflect.Array.newInstance(Class::class.java, 0).javaClass
                    )
                ) as java.lang.reflect.Method
                
                // 4. Use the reflected getDeclaredMethod to load dalvik.system.VMRuntime
                val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
                
                // 5. Use the reflected getDeclaredMethod to load VMRuntime.getRuntime
                val getRuntime = getDeclaredMethodReflect.invoke(
                    vmRuntimeClass,
                    "getRuntime",
                    null
                ) as java.lang.reflect.Method
                
                // 6. Use the reflected getDeclaredMethod to load VMRuntime.setHiddenApiExemptions
                val setHiddenApiExemptions = getDeclaredMethodReflect.invoke(
                    vmRuntimeClass,
                    "setHiddenApiExemptions",
                    arrayOf(java.lang.reflect.Array.newInstance(String::class.java, 0).javaClass)
                ) as java.lang.reflect.Method
                
                // 7. Get the VMRuntime instance
                val vmRuntime = getRuntime.invoke(null)
                
                // 8. Invoke setHiddenApiExemptions with "L" to exempt all APIs
                val exemptions = arrayOf("L")
                setHiddenApiExemptions.invoke(vmRuntime, arrayOf<Any>(exemptions))
                
                Log.d("HiddenApiBypass", "🟢 Successfully bypassed hidden API restrictions via double reflection!")
            } catch (e: Exception) {
                Log.e("HiddenApiBypass", "🔴 Failed to bypass hidden API restrictions", e)
            }
        }
    }
}
