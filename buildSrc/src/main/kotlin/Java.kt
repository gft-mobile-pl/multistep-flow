import org.gradle.api.JavaVersion

object Java {
    val sourceCompatibility = JavaVersion.VERSION_17
    val targetCompatibility = JavaVersion.VERSION_17
    val jvmTarget = JavaVersion.VERSION_17.toString()
}
