package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, ProjectEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WorkspaceDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: WorkspaceDatabase? = null

        fun getInstance(context: Context): WorkspaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkspaceDatabase::class.java,
                    "ai_workspace.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDemoData(getInstance(context).workspaceDao())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDemoData(dao: WorkspaceDao) {
            val now = System.currentTimeMillis()
            val oneHourAgo = now - 3600_000L
            val yesterday = now - 86400_000L
            val threeDaysAgo = now - (86400_000L * 3)

            // 1. Projects
            val project1 = ProjectEntity(
                id = "proj-tbs-incentive",
                name = "TBS Incentive",
                description = "ระบบและสูตรการคำนวณ Incentive ฝ่ายขายและตัวแทนจำหน่าย",
                instructions = "Do not modify the locked calculation engine. เน้นการตรวจสอบสูตรการคำนวณ incentive และ validation ของยอดขาย ให้ผลลัพธ์เป็นโครงสร้างตารางที่ชัดเจน",
                createdAt = yesterday,
                updatedAt = now,
                colorHex = "#38BDF8",
                iconName = "calculate"
            )

            val project2 = ProjectEntity(
                id = "proj-workstation",
                name = "Workstation ABBTG-A",
                description = "การตรวจสอบสถานะและการทำงานของอุปกรณ์ในสายการผลิต",
                instructions = "ระบบตรวจสอบสายการผลิต Workstation ABBTG-A ให้ความสำคัญกับความปลอดภัย ค่าการสั่นสะเทือน อุณหภูมิ และมาตรฐานการบำรุงรักษาเชิงป้องกัน (Predictive Maintenance)",
                createdAt = threeDaysAgo,
                updatedAt = yesterday,
                colorHex = "#10B981",
                iconName = "precision_manufacturing"
            )

            val project3 = ProjectEntity(
                id = "proj-ai-research",
                name = "AI Research",
                description = "วิจัยและพัฒนาโมเดล AI ในองค์กร",
                instructions = "การวิจัยและประยุกต์ใช้โมเดล Gemini สำหรับงานวิเคราะห์ข้อความและการเขียนโค้ด ให้คำตอบเป็นภาษาไทยอย่างเป็นทางการและถูกต้องตามหลักวิชาการ",
                createdAt = threeDaysAgo,
                updatedAt = threeDaysAgo,
                colorHex = "#8B5CF6",
                iconName = "psychology"
            )

            dao.insertProject(project1)
            dao.insertProject(project2)
            dao.insertProject(project3)

            // 2. Conversations
            val conv1 = ConversationEntity(
                id = "conv-calc-engine",
                title = "ตรวจสอบ Calculation Engine",
                createdAt = oneHourAgo,
                updatedAt = now,
                projectId = project1.id,
                modelId = "gemini-3.5-flash",
                isPinned = true
            )

            val conv2 = ConversationEntity(
                id = "conv-abbtg-spec",
                title = "Workstation ABBTG-A Checklist",
                createdAt = yesterday,
                updatedAt = yesterday,
                projectId = project2.id,
                modelId = "gemini-3.5-flash"
            )

            val conv3 = ConversationEntity(
                id = "conv-ai-roadmap",
                title = "วางแผนฟีเจอร์ AI Workspace",
                createdAt = threeDaysAgo,
                updatedAt = threeDaysAgo,
                projectId = project3.id,
                modelId = "gemini-3.5-flash"
            )

            dao.insertConversation(conv1)
            dao.insertConversation(conv2)
            dao.insertConversation(conv3)

            // 3. Messages
            dao.insertMessage(
                MessageEntity(
                    id = "msg-1-1",
                    conversationId = conv1.id,
                    role = "user",
                    content = "ช่วยตรวจสอบโครงสร้าง Calculation Engine ของ Incentive หน่อยว่าต้องเตรียมเงื่อนไขอะไรบ้าง",
                    timestamp = oneHourAgo
                )
            )

            dao.insertMessage(
                MessageEntity(
                    id = "msg-1-2",
                    conversationId = conv1.id,
                    role = "model",
                    content = """จากการตรวจสอบข้อกำหนดของโปรเจกต์ **TBS Incentive** (มีเงื่อนไขล็อค: *Do not modify the locked calculation engine*):

### 1. ปัจจัยหลักที่ใช้ในการคำนวณ:
- **Baseline Target (เป้าหมายพื้นฐาน):** ยอดขายขั้นต่ำที่ต้องทำได้ก่อนเริ่มคิด Incentive
- **Tiered Multiplier (อัตราขั้นบันได):**
  - ขั้นที่ 1 (100% - 110%): คูณอัตรา 1.2x
  - ขั้นที่ 2 (111% - 130%): คูณอัตรา 1.5x
  - ขั้นที่ 3 (> 130%): คูณอัตรา 2.0x (สูงสุดตาม Cap)
- **Validation Rules:**
  - ยอดขายต้องได้รับการอนุมัติ (Status = Approved)
  - ไม่มีการคืนสินค้าหรือยกเลิกสัญญาภายใน 30 วัน

คำนวณตามสูตรหลักของ Engine เดิมอย่างเคร่งครัดโดยไม่กระทบ Logic ภายในครับ""",
                    timestamp = oneHourAgo + 2000L
                )
            )

            dao.insertMessage(
                MessageEntity(
                    id = "msg-2-1",
                    conversationId = conv2.id,
                    role = "user",
                    content = "ขอ Checklist ตรวจสอบประจำวันของ Workstation ABBTG-A",
                    timestamp = yesterday
                )
            )

            dao.insertMessage(
                MessageEntity(
                    id = "msg-2-2",
                    conversationId = conv2.id,
                    role = "model",
                    content = """Checklist การตรวจสอบประจำวันสำหรับ **Workstation ABBTG-A**:

- [ ] **1. อุณหภูมิแกนหมุน (Spindle Temp):** ตรวจสอบไม่ให้เกิน 65°C
- [ ] **2. การสั่นสะเทือน (Vibration Level):** ค่า RMS ต่ำกว่า 2.5 mm/s
- [ ] **3. แรงดันไฮดรอลิก (Hydraulic Pressure):** ค่าคงที่ 140 - 150 bar
- [ ] **4. สวิตช์นิรภัยฉุกเฉิน (E-Stop):** ทดสอบการกดหยุดและการตัดไฟทันที
- [ ] **5. การเชื่อมต่อเซนเซอร์ PLC:** ตรวจสอบไฟสถานะโมดูลปกติ ไม่มี Error Code""",
                    timestamp = yesterday + 2000L
                )
            )
        }
    }
}
