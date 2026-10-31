plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}

tasks.register<Exec>("validateHadithDataset") {
    group = "verification"
    description = "Validates the integrity of the Hadith dataset"
    commandLine("python3", "${rootDir}/scripts/validate_hadith_dataset.py")
}

tasks.register<Exec>("validatePrayerDataset") {
    group = "verification"
    description = "Validates the integrity of the Bacaan Sholat dataset"
    commandLine("python3", "${rootDir}/scripts/validate_prayer_dataset.py")
}

tasks.register<Exec>("validateDailyPrayerDataset") {
    group = "verification"
    description = "Validates the integrity of the Doa Harian dataset"
    commandLine("python3", "${rootDir}/scripts/validate_daily_prayer_dataset.py")
}

tasks.register<Exec>("validateAsmaulHusnaDataset") {
    group = "verification"
    description = "Validates the integrity of the 99 Asmaul Husna dataset"
    commandLine("python3", "${rootDir}/scripts/validate_asmaul_husna_dataset.py")
}

// quran.db (~99 MB) dipecah jadi potongan agar setiap file bisa diunggah.
// Potongan di app/db_parts/ digabung otomatis menjadi app/src/main/assets/quran.db.
val quranDbParts = (1..8).map { rootProject.file("app/db_parts/alquran$it.db") }
val quranDbOutput = rootProject.file("app/src/main/assets/quran.db")

tasks.register("joinQuranDb") {
    group = "verification"
    description = "Menggabungkan potongan alquran1.db..alquran8.db menjadi quran.db"
    inputs.files(quranDbParts)
    outputs.file(quranDbOutput)
    doLast {
        quranDbOutput.parentFile.mkdirs()
        quranDbOutput.outputStream().use { out ->
            quranDbParts.forEach { part -> part.inputStream().use { it.copyTo(out) } }
        }
    }
}

tasks.register<Exec>("validateDataset") {
    group = "verification"
    description = "Validates the integrity of the Al-Qur'an dataset (114 surahs, 6,236 ayahs, 30 juz)"
    commandLine("python3", "${rootDir}/scripts/validate_dataset.py")
    dependsOn("joinQuranDb", "validateHadithDataset", "validatePrayerDataset", "validateDailyPrayerDataset", "validateAsmaulHusnaDataset")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
