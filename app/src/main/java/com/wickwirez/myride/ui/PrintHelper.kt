package com.wickwirez.myride.ui

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.wickwirez.myride.model.ServiceRecord
import com.wickwirez.myride.model.Vehicle
import java.text.SimpleDateFormat
import java.util.Locale

object PrintHelper {

    fun printServiceHistory(context: Context, vehicle: Vehicle, records: List<ServiceRecord>) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val jobName = "${vehicle.year} ${vehicle.make} ${vehicle.model} Service History"
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val adapter = view.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, buildHtml(vehicle, records), "text/html", "UTF-8", null)
    }

    private fun buildHtml(vehicle: Vehicle, records: List<ServiceRecord>): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val vehicleName = vehicle.nickname.ifBlank { "${vehicle.year} ${vehicle.make} ${vehicle.model}" }

        val rows = records.joinToString("") { r ->
            """
            <tr>
                <td>${dateFormat.format(r.date)}</td>
                <td>${r.type.name.replace('_', ' ')}</td>
                <td>${r.mileage} mi</td>
                <td>${if (r.cost > 0) String.format(Locale.US, "$%.2f", r.cost) else "—"}</td>
                <td>${r.shopName}</td>
                <td>${r.notes}</td>
            </tr>
            """.trimIndent()
        }

        return """
            <html>
            <head>
                <style>
                    body { font-family: sans-serif; padding: 16px; }
                    h1 { font-size: 20px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 16px; }
                    th, td { border: 1px solid #ccc; padding: 8px; text-align: left; font-size: 12px; }
                    th { background-color: #eee; }
                </style>
            </head>
            <body>
                <h1>$vehicleName</h1>
                <p>${vehicle.year} ${vehicle.make} ${vehicle.model} ${vehicle.trim}</p>
                <p>Current mileage: ${vehicle.currentMileage}</p>
                ${if (vehicle.vin.isNotBlank()) "<p>VIN: ${vehicle.vin}</p>" else ""}
                <table>
                    <tr><th>Date</th><th>Type</th><th>Mileage</th><th>Cost</th><th>Shop</th><th>Notes</th></tr>
                    $rows
                </table>
            </body>
            </html>
        """.trimIndent()
    }
}
