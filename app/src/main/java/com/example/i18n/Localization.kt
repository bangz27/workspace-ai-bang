package com.example.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class Language(val code: String, val displayName: String) {
    TH("th", "ไทย"),
    EN("en", "English")
}

val LocalAppLanguage = compositionLocalOf { Language.TH }

object I18n {
    private val thStrings = mapOf(
        // Navigation & General
        "app.name" to "AI Workspace",
        "chat.newChat" to "แชทใหม่",
        "chat.search" to "ค้นหา",
        "chat.searchChats" to "ค้นหาแชท...",
        "chat.projects" to "โปรเจกต์",
        "chat.settings" to "ตั้งค่า",
        "chat.send" to "ส่ง",
        "chat.typeMessage" to "พิมพ์ข้อความ...",
        "chat.uploadFile" to "อัปโหลดไฟล์",
        "chat.generating" to "กำลังสร้างคำตอบ...",
        "chat.somethingWrong" to "เกิดข้อผิดพลาด กรุณาลองอีกครั้ง",
        "chat.missingApiKey" to "ยังไม่ได้ตั้งค่า Gemini API กรุณาตรวจสอบการตั้งค่า",
        "chat.copy" to "คัดลอก",
        "chat.copied" to "คัดลอกแล้ว",
        "chat.regenerate" to "สร้างคำตอบใหม่",
        "chat.share" to "แชร์",
        "chat.export" to "ส่งออก",
        "chat.exportMarkdown" to "ส่งออกเป็น Markdown",
        "chat.exportText" to "ส่งออกเป็นข้อความ",
        "chat.offline" to "ออฟไลน์",
        "chat.retry" to "ลองอีกครั้ง",
        "chat.voiceTap" to "แตะเพื่อพูด",
        "chat.voiceListening" to "กำลังบันทึก...",
        "chat.voiceError" to "ไม่สามารถรับฟังเสียงได้ในขณะนี้",
        "chat.emptyTitle" to "ผู้ช่วย AI ประจำพื้นที่ทำงานของคุณ",
        "chat.emptySubtitle" to "ถามคำถาม วางแผนงาน วิเคราะห์ข้อมูล หรือเลือกโปรเจกต์เพื่อเริ่มงาน",
        "chat.projectContextBadge" to "โปรเจกต์: ",
        "chat.noMessages" to "เริ่มต้นการสนทนาได้เลย",

        // Time Groups
        "history.today" to "วันนี้",
        "history.yesterday" to "เมื่อวาน",
        "history.last7Days" to "7 วันที่ผ่านมา",
        "history.older" to "เก่ากว่านั้น",
        "history.empty" to "ไม่มีประวัติการสนทนา",

        // Actions
        "action.summarize" to "สรุปข้อมูล",
        "action.checklist" to "สร้าง Checklist",
        "action.document" to "สร้างเอกสาร",
        "action.analyze" to "วิเคราะห์ไฟล์",
        "action.table" to "สร้างตาราง",
        "action.code" to "สร้างโค้ด",

        // Projects
        "project.title" to "โปรเจกต์ของคุณ",
        "project.new" to "สร้างโปรเจกต์ใหม่",
        "project.nameLabel" to "ชื่อโปรเจกต์",
        "project.descLabel" to "คำอธิบายโดยย่อ",
        "project.instructionsLabel" to "คำสั่งเฉพาะของโปรเจกต์ (Custom Instructions)",
        "project.instructionsHint" to "เช่น ไม่แก้ไข Calculation Engine, ให้ตอบแบบสรุปประเด็นกระชับ, เน้นสูตรคำนวณ",
        "project.save" to "บันทึกโปรเจกต์",
        "project.cancel" to "ยกเลิก",
        "project.delete" to "ลบโปรเจกต์",
        "project.conversations" to "การสนทนาในโปรเจกต์นี้",
        "project.empty" to "ยังไม่มีโปรเจกต์ สร้างโปรเจกต์เพื่อจัดการบริบทการทำงานเฉพาะด้าน",
        "project.active" to "ใช้งานอยู่",
        "project.useThis" to "เปิดในโปรเจกต์นี้",

        // Settings
        "settings.title" to "ตั้งค่า",
        "settings.appearance" to "รูปลักษณ์",
        "settings.theme" to "ธีม",
        "settings.themeLight" to "สว่าง",
        "settings.themeDark" to "มืด",
        "settings.themeSystem" to "ตามระบบ",
        "settings.language" to "ภาษา",
        "settings.aiSection" to "การตั้งค่า AI",
        "settings.modelDefault" to "โมเดลเริ่มต้น",
        "settings.modelFast" to "เร็ว (Gemini 3.5 Flash)",
        "settings.modelAdvanced" to "ขั้นสูง (Gemini 3.1 Pro)",
        "settings.customApiKey" to "Gemini API Key (กำหนดเอง)",
        "settings.apiKeyHint" to "ใส่ API Key สำรองหากต้องการ หรือใช้ค่าระบบ",
        "settings.saveApiKey" to "บันทึก Key",
        "settings.dataSection" to "ข้อมูล",
        "settings.exportData" to "ส่งออกข้อมูลทั้งหมด",
        "settings.clearData" to "ล้างข้อมูลในเครื่อง",
        "settings.resetDemo" to "โหลดข้อมูลตัวอย่างเริ่มต้น (Demo Data)",
        "settings.appSection" to "แอปพลิเคชัน",
        "settings.version" to "เวอร์ชัน 1.0.0 (AI Workspace)",
        "settings.offlineStatus" to "สถานะเครือข่าย: ออนไลน์",

        // Search
        "search.title" to "ค้นหาแชทและโปรเจกต์",
        "search.placeholder" to "ค้นหาหัวข้อ ข้อความ หรือโปรเจกต์...",
        "search.noResults" to "ไม่พบผลลัพธ์ที่ตรงกับการค้นหา",
        "search.resultsCount" to "พบ %d ผลลัพธ์"
    )

    private val enStrings = mapOf(
        // Navigation & General
        "app.name" to "AI Workspace",
        "chat.newChat" to "New Chat",
        "chat.search" to "Search",
        "chat.searchChats" to "Search chats...",
        "chat.projects" to "Projects",
        "chat.settings" to "Settings",
        "chat.send" to "Send",
        "chat.typeMessage" to "Type a message...",
        "chat.uploadFile" to "Upload file",
        "chat.generating" to "Generating response...",
        "chat.somethingWrong" to "Something went wrong. Please try again.",
        "chat.missingApiKey" to "Gemini API Key is not configured. Please check settings.",
        "chat.copy" to "Copy",
        "chat.copied" to "Copied",
        "chat.regenerate" to "Regenerate",
        "chat.share" to "Share",
        "chat.export" to "Export",
        "chat.exportMarkdown" to "Export as Markdown",
        "chat.exportText" to "Export as Plain Text",
        "chat.offline" to "Offline",
        "chat.retry" to "Retry",
        "chat.voiceTap" to "Tap to speak",
        "chat.voiceListening" to "Listening...",
        "chat.voiceError" to "Speech recognition currently unavailable",
        "chat.emptyTitle" to "Your AI Workspace Assistant",
        "chat.emptySubtitle" to "Ask questions, plan workflows, analyze data, or select a project to begin.",
        "chat.projectContextBadge" to "Project: ",
        "chat.noMessages" to "Start a new conversation now",

        // Time Groups
        "history.today" to "Today",
        "history.yesterday" to "Yesterday",
        "history.last7Days" to "Previous 7 Days",
        "history.older" to "Older",
        "history.empty" to "No conversation history",

        // Actions
        "action.summarize" to "Summarize",
        "action.checklist" to "Create Checklist",
        "action.document" to "Draft Document",
        "action.analyze" to "Analyze Content",
        "action.table" to "Generate Table",
        "action.code" to "Generate Code",

        // Projects
        "project.title" to "Your Projects",
        "project.new" to "Create New Project",
        "project.nameLabel" to "Project Name",
        "project.descLabel" to "Brief Description",
        "project.instructionsLabel" to "Project Instructions (Custom Context)",
        "project.instructionsHint" to "e.g., Do not modify calculation engine, keep answers concise",
        "project.save" to "Save Project",
        "project.cancel" to "Cancel",
        "project.delete" to "Delete Project",
        "project.conversations" to "Conversations in this project",
        "project.empty" to "No projects yet. Create one to organize custom context.",
        "project.active" to "Active",
        "project.useThis" to "Open in this project",

        // Settings
        "settings.title" to "Settings",
        "settings.appearance" to "Appearance",
        "settings.theme" to "Theme",
        "settings.themeLight" to "Light",
        "settings.themeDark" to "Dark",
        "settings.themeSystem" to "System",
        "settings.language" to "Language",
        "settings.aiSection" to "AI Configuration",
        "settings.modelDefault" to "Default Model",
        "settings.modelFast" to "Fast (Gemini 3.5 Flash)",
        "settings.modelAdvanced" to "Advanced (Gemini 3.1 Pro)",
        "settings.customApiKey" to "Custom Gemini API Key",
        "settings.apiKeyHint" to "Optional override if not using environment key",
        "settings.saveApiKey" to "Save Key",
        "settings.dataSection" to "Data",
        "settings.exportData" to "Export All Data",
        "settings.clearData" to "Clear Local Storage",
        "settings.resetDemo" to "Reset to Demo Data",
        "settings.appSection" to "Application",
        "settings.version" to "Version 1.0.0 (AI Workspace)",
        "settings.offlineStatus" to "Network Status: Online",

        // Search
        "search.title" to "Search Chats & Projects",
        "search.placeholder" to "Search titles, messages, or projects...",
        "search.noResults" to "No matching results found",
        "search.resultsCount" to "Found %d results"
    )

    fun get(key: String, language: Language = Language.TH): String {
        return when (language) {
            Language.TH -> thStrings[key] ?: enStrings[key] ?: key
            Language.EN -> enStrings[key] ?: thStrings[key] ?: key
        }
    }
}

@Composable
fun t(key: String): String {
    val currentLang = LocalAppLanguage.current
    return I18n.get(key, currentLang)
}
