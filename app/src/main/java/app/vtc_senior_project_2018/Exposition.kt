package app.vtc_senior_project_2018

/**
 * 9/27/2017.
 */
data class Exposition(val id:Int,
                      val name: String,
                      val type: ExpositionType)

enum class ExpositionType {
    IMAGE, AUDIO, TEXT
}