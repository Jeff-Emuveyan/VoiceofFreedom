package com.bellogate.voiceoffreedom.ui

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bellogate.voiceoffreedom.R
import kotlinx.android.synthetic.main.fragment_webview.*

/**
 * A simple [Fragment] subclass.
 */
class WebViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true

        // Enable Javascript
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true

        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i(WebViewFragment::class.java.simpleName, "Loading finished")
                progressBar.visibility = View.INVISIBLE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.i(WebViewFragment::class.java.simpleName, "Loading started")
                progressBar.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                progressBar.visibility = View.INVISIBLE
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError?
            ) {
                progressBar.visibility = View.INVISIBLE
                var builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Connection error, try again");
                builder.setPositiveButton("continue") {
                        dialog, which -> handler.proceed();
                }

                builder.setNegativeButton("cancel"
                ) { dialog, which -> handler.cancel(); };
                val dialog = builder.create()
                dialog.show();
            }
        }


        //This is actually bad programming practice sha. A Fragment not suppose to call the Activity like so.
        // But we can get away with it since this is just an ordinary webview:
        if(MainActivity.navView.checkedItem!!.itemId == R.id.nav_live){
            webView.loadUrl("https://www.facebook.com/vfmglobal/live")
        }else if(MainActivity.navView.checkedItem!!.itemId == R.id.nav_branches){
            webView.loadUrl("https://vfmglobal.org/branches/")
        }else{
            webView.loadUrl("https://vfmglobal.org/")
        }
    }
}
