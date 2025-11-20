package uz.devlooper.androidtvlauncher.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import uz.devlooper.androidtvlauncher.R
import uz.devlooper.androidtvlauncher.databinding.ActivityMainBinding
import uz.devlooper.androidtvlauncher.utils.log
import uz.devlooper.androidtvlauncher.services.AppResolver
import uz.devlooper.androidtvlauncher.services.DefaultLauncherHelper
import uz.devlooper.androidtvlauncher.services.SharedPreference

const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var defaultLauncherHelper: DefaultLauncherHelper
    private val adapter = AppsAdapter()

    private var accessibilityLaunchListener =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.log(TAG)
            result.resultCode
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        defaultLauncherHelper = DefaultLauncherHelper(this)

        binding.appsRv.adapter = adapter
        val applications = AppResolver().getApplications(this)
        applications.log(TAG)
        val filter = applications.filter { appItemData ->
            appItemData.displayName != getString(R.string.app_name)
        }.sortedBy { it.displayName }

        adapter.submitList(filter.asReversed())

        adapter.setOnIteClickListener { app ->
            val launchIntentUri = app.launchIntentUriLeanback ?: app.launchIntentUriDefault
            startActivity(Intent.parseUri(launchIntentUri, 0))
        }

        binding.accessibilityService.setOnClickListener(this)
        binding.setAsDefault.setOnClickListener(this)
        binding.settings.setOnClickListener(this)

        binding.launcherCheckbox.isChecked = SharedPreference.isLauncherByUser

        binding.launcherCheckbox.setOnCheckedChangeListener { view, isChecked ->
            SharedPreference.isLauncherByUser = isChecked
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.accessibility_service -> openAccessibilitySettings()
            R.id.set_as_default -> {
                if (defaultLauncherHelper.canRequestDefaultLauncher()) {
                    if (!defaultLauncherHelper.isDefaultLauncher()) {
                        val intent = defaultLauncherHelper.requestDefaultLauncherIntent()
                        if (intent != null) {
                            accessibilityLaunchListener.launch(intent)
                        }
                    } else {
                        Toast.makeText(this, "App already set as default!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            R.id.settings -> {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        if (!SharedPreference.isLauncherByUser) {
            super.onBackPressed()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val resolveInfo = intent.resolveActivity(this.packageManager)
        if (resolveInfo != null) {
            try {
                accessibilityLaunchListener.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Accessibility sozlamalari topilmadi", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(this, "Accessibility sozlamalari mavjud emas", Toast.LENGTH_SHORT)
                .show()
        }
    }

}