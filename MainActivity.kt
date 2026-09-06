package com.expenseanalyzer.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var root: LinearLayout
    private lateinit var web: WebView
    private val prefs by lazy { getSharedPreferences("expense_app", Context.MODE_PRIVATE) }

    // First launch asks for the deployed Apps Script /exec URL. It is then stored on this phone.
    private val defaultUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGate()
    }

    private fun showGate() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(0xFFF5F7FC.toInt())
        }
        val title = TextView(this).apply {
            text = "💰\nMy Expense Analyzer"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(0xFF10203A.toInt())
            setPadding(0, 20, 0, 28)
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        val subtitle = TextView(this).apply {
            text = "Unlock with fingerprint or PIN"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(0xFF64748B.toInt())
        }
        root.addView(subtitle)

        val bio = Button(this).apply {
            text = "👆  Use Fingerprint / Face"
            textSize = 17f
            isAllCaps = false
            setOnClickListener { biometricUnlock() }
        }
        root.addView(bio, LinearLayout.LayoutParams(-1, 64).apply { topMargin = 30 })

        val pin = Button(this).apply {
            text = "🔢  Use PIN"
            textSize = 17f
            isAllCaps = false
            setOnClickListener { pinDialog() }
        }
        root.addView(pin, LinearLayout.LayoutParams(-1, 64).apply { topMargin = 14 })

        val url = prefs.getString("url", defaultUrl).orEmpty()
        if (url.isEmpty()) {
            val setup = TextView(this).apply {
                text = "First use: tap here to set the deployed /exec link"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(0xFF64748B.toInt())
                setPadding(0, 30, 0, 0)
                setOnClickListener { urlDialog() }
            }
            root.addView(setup, LinearLayout.LayoutParams(-1, -2))
        } else {
            val change = TextView(this).apply {
                text = "Change web-app link"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(0xFF2563EB.toInt())
                setPadding(0, 24, 0, 0)
                setOnClickListener { urlDialog() }
            }
            root.addView(change, LinearLayout.LayoutParams(-1, -2))
        }
        setContentView(root)
    }

    private fun biometricUnlock() {
        val bm = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (bm.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Fingerprint/face is not available on this phone. Use PIN.", Toast.LENGTH_LONG).show()
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { openWebApp() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON && errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    Toast.makeText(this@MainActivity, errString, Toast.LENGTH_SHORT).show()
                }
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("My Expense Analyzer")
            .setSubtitle("Authenticate to open your expense tracker")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    private fun pinDialog() {
        // Use the existing server-side app PIN. This avoids maintaining a second PIN in the APK.
        openWebApp()
    }

    private fun urlDialog() {
        val input = EditText(this).apply {
            inputType = 33
            hint = "https://script.google.com/macros/s/.../exec"
            setText(prefs.getString("url", "") ?: "")
        }
        AlertDialogBuilder(this, "Web App Link", input) {
            val u = input.text.toString().trim()
            if (u.startsWith("https://script.google.com/macros/s/") && u.endsWith("/exec")) {
                prefs.edit().putString("url", u).apply(); showGate()
            } else Toast.makeText(this, "Please enter the complete Apps Script /exec link", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebApp() {
        val url = prefs.getString("url", defaultUrl).orEmpty()
        if (url.isEmpty()) { urlDialog(); return }
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.cacheMode = WebSettings.LOAD_DEFAULT
        web.settings.setSupportZoom(false)
        web.webViewClient = WebViewClient()
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        setContentView(web)
        web.loadUrl(url)
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    private fun AlertDialogBuilder(context: Context, title: String, input: EditText, action: () -> Unit) {
        android.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ -> action() }
            .show()
    }
}
