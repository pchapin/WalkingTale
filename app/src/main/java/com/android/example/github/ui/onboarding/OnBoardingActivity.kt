package com.android.example.github.ui.onboarding

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast

import agency.tango.materialintroscreen.MaterialIntroActivity
import agency.tango.materialintroscreen.MessageButtonBehaviour
import agency.tango.materialintroscreen.SlideFragmentBuilder
import com.android.example.github.R

class OnBoardingActivity : MaterialIntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableLastSlideAlphaExitTransition(true)

        backButtonTranslationWrapper
                .setEnterTranslation { view, percentage -> view.alpha = percentage }

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.first_slide_background)
                .buttonsColor(R.color.first_slide_buttons)
                .image(R.drawable.eye)
                .title("Organize your time with us")
                .description("Would you try?")
                .build(),
                MessageButtonBehaviour(View.OnClickListener { showMessage("We provide solutions to make you love your work") }, "Work with love"))

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.second_slide_background)
                .buttonsColor(R.color.second_slide_buttons)
                .title("Want more?")
                .description("Go on")
                .build())

        addSlide(CustomSlide())

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.third_slide_background)
                .buttonsColor(R.color.third_slide_buttons)
                .neededPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                .image(R.drawable.sync)
                .title("We provide best tools")
                .description("ever")
                .build(),
                MessageButtonBehaviour(View.OnClickListener { showMessage("Try us!") }, "Tools"))

        addSlide(SlideFragmentBuilder()
                .backgroundColor(R.color.fourth_slide_background)
                .buttonsColor(R.color.fourth_slide_buttons)
                .title("That's it")
                .description("Would you join us?")
                .build())
    }

    override fun onFinish() {
        super.onFinish()
        Toast.makeText(this, "Try this library in your project! :)", Toast.LENGTH_SHORT).show()
    }
}