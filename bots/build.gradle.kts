dependencies {
    implementation(project(":game"))
    implementation(project(mapOf("path" to ":bots:ljedmitry-bot")))
    implementation(project(mapOf("path" to ":bots:melniknow-bots")))
    implementation(project(mapOf("path" to ":bots:yurkevich-bots")))
}