package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

const val SUPER_ADMIN_UID = "PMXvtEnXBacRZDKoVBWUk1XFsmw1"
const val GUEST_USER_ID = "guest_local"

@Keep
@IgnoreExtraProperties
data class AppConfig(
    @DocumentId val id: String = "settings",
    val appTitle: String = "স্ট্রবেরি চাষি বন্ধু",
    val welcomeBanner: String = "আমাদের লক্ষ্য: বেশি ফল, বেশি লাভ",
    val dashboardSubtitle: String = "সুস্থ গাছে সমৃদ্ধ ফসল",
    val irrigationIntervalDays: Long = 4L,
    val workTypes: List<String> = listOf(
        "নিড়ানি দেওয়া",
        "সার দেওয়া",
        "মালচিং/পলিথিন",
        "ফল তোলা",
        "অন্যান্য"
    ),
    val fertilizerPresets: List<String> = listOf(
        "ইউরিয়া",
        "টিএসপি (TSP)",
        "এমওপি (MOP)",
        "ডিএপি (DAP)",
        "বোরন",
        "জিংক",
        "সরিষার খৈল",
        "হরলিক্স ও গুড়"
    ),
    val sprayPresets: List<String> = listOf(
        "ম্যানকোজেব",
        "কার্বেন্ডাজিম",
        "ভার্টিমেক",
        "হরলিক্স + গুড় স্প্রে",
        "বোরন + অনুখাদ্য",
        "ছত্রাকনাশক স্প্রে"
    ),
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FeedingRecipe(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "",
    val summary: String = "",
    val dosage: String = "",
    val steps: List<String> = emptyList(),
    val tip: String = "",
    val orderIndex: Long = 0L,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FarmArticle(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "সার প্রয়োগ",
    val summary: String = "",
    val body: String = "",
    val publishedDateLabel: String = "",
    val orderIndex: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CustomFeedingPlan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val method: String = "",
    val dosage: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val dateLabel: String = "",
    val nextDateMillis: Long = 0L,
    val nextDateLabel: String = "",
    val notes: String = "",
    val trashed: Boolean = false,
    val trashedAtMillis: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val farmName: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FertilizerPurchase(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val dateLabel: String = "",
    val fertilizerName: String = "",
    val shopName: String = "",
    val quantity: String = "",
    val totalPrice: Double = 0.0,
    val due: Boolean = false,
    val notes: String = "",
    val trashed: Boolean = false,
    val trashedAtMillis: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class IrrigationLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val dateLabel: String = "",
    val plotName: String = "",
    val cost: Double = 0.0,
    val nextIrrigationMillis: Long = 0L,
    val nextIrrigationLabel: String = "",
    val notes: String = "",
    val trashed: Boolean = false,
    val trashedAtMillis: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SprayLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val dateLabel: String = "",
    val medicineName: String = "",
    val dose: String = "",
    val cost: Double = 0.0,
    val nextSprayMillis: Long = 0L,
    val nextSprayLabel: String = "",
    val notes: String = "",
    val trashed: Boolean = false,
    val trashedAtMillis: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LaborLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val dateLabel: String = "",
    val workerCount: Long = 1L,
    val dailyWage: Double = 0.0,
    val totalCost: Double = 0.0,
    val workType: String = "",
    val notes: String = "",
    val trashed: Boolean = false,
    val trashedAtMillis: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class HarvestSale(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val dateLabel: String = "",
    val weightKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val totalAmount: Double = 0.0,
    val buyerName: String = "",
    val notes: String = "",
    val trashed: Boolean = false,
    val trashedAtMillis: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

enum class RecordCategory(val bengaliLabel: String) {
    FERTILIZER("সার কেনা"),
    FEEDING_PLAN("সারের ডোজ"),
    IRRIGATION("সেচ"),
    SPRAY("স্প্রে"),
    LABOR("শ্রমিক"),
    HARVEST("ফসল বিক্রি")
}

data class TrashItem(
    val id: String,
    val category: RecordCategory,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val dateLabel: String,
    val trashedAtMillis: Long
)

data class LedgerEntry(
    val id: String,
    val category: RecordCategory,
    val title: String,
    val detail: String,
    val amount: Double,
    val isIncome: Boolean,
    val isDue: Boolean = false,
    val dateMillis: Long,
    val dateLabel: String
)

data class FarmSummary(
    val fertilizerTotal: Double = 0.0,
    val fertilizerDue: Double = 0.0,
    val irrigationTotal: Double = 0.0,
    val sprayTotal: Double = 0.0,
    val laborTotal: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalHarvestKg: Double = 0.0,
    val netProfit: Double = 0.0,
    val nextIrrigationLabel: String = "",
    val nextIrrigationPlot: String = "",
    val nextSprayLabel: String = "",
    val nextSprayMedicine: String = "",
    val trashedCount: Int = 0
)

sealed interface UndoAction {
    data class AddedFertilizer(val item: FertilizerPurchase) : UndoAction
    data class EditedFertilizer(val before: FertilizerPurchase, val after: FertilizerPurchase) : UndoAction
    data class TrashedFertilizer(val item: FertilizerPurchase) : UndoAction
    data class RestoredFertilizer(val item: FertilizerPurchase) : UndoAction
    data class PaidFertilizerDue(val before: FertilizerPurchase, val after: FertilizerPurchase) : UndoAction

    data class AddedFeedingPlan(val item: CustomFeedingPlan) : UndoAction
    data class EditedFeedingPlan(val before: CustomFeedingPlan, val after: CustomFeedingPlan) : UndoAction
    data class TrashedFeedingPlan(val item: CustomFeedingPlan) : UndoAction
    data class RestoredFeedingPlan(val item: CustomFeedingPlan) : UndoAction

    data class AddedIrrigation(val item: IrrigationLog) : UndoAction
    data class EditedIrrigation(val before: IrrigationLog, val after: IrrigationLog) : UndoAction
    data class TrashedIrrigation(val item: IrrigationLog) : UndoAction
    data class RestoredIrrigation(val item: IrrigationLog) : UndoAction

    data class AddedSpray(val item: SprayLog) : UndoAction
    data class EditedSpray(val before: SprayLog, val after: SprayLog) : UndoAction
    data class TrashedSpray(val item: SprayLog) : UndoAction
    data class RestoredSpray(val item: SprayLog) : UndoAction

    data class AddedLabor(val item: LaborLog) : UndoAction
    data class EditedLabor(val before: LaborLog, val after: LaborLog) : UndoAction
    data class TrashedLabor(val item: LaborLog) : UndoAction
    data class RestoredLabor(val item: LaborLog) : UndoAction

    data class AddedHarvest(val item: HarvestSale) : UndoAction
    data class EditedHarvest(val before: HarvestSale, val after: HarvestSale) : UndoAction
    data class TrashedHarvest(val item: HarvestSale) : UndoAction
    data class RestoredHarvest(val item: HarvestSale) : UndoAction
}

object DefaultFeedingRecipes {
    val items: List<FeedingRecipe> = listOf(
        FeedingRecipe(
            id = "default_pocket_gonj",
            title = "পকেট গঞ্জ পদ্ধতিতে শুকনো সার প্রয়োগ",
            category = "শুকনো সার",
            summary = "গাছের গোড়া থেকে ৩-৪ ইঞ্চি দূরে ছোট গর্ত (পকেট) করে ইউরিয়া, টিএসপি, এমওপি ও বোরন সার দিলে গাছ তাড়াতাড়ি পুষ্টি পায় এবং সার নষ্ট হয় না।",
            dosage = "প্রতি গাছে মিশ্র সার ৫-৭ গ্রাম (চা চামচের ১ চামচ)",
            steps = listOf(
                "প্রতি ১০০ গ্রাম ইউরিয়া + ১০০ গ্রাম টিএসপি + ৮০ গ্রাম এমওপি + ১০ গ্রাম বোরন ভালোভাবে মিশিয়ে নিন।",
                "গাছের গোড়া থেকে ৩-৪ ইঞ্চি পাশে আঙুল বা কাঠি দিয়ে ২ ইঞ্চি গভীর গর্ত (পকেট গঞ্জ) তৈরি করুন।",
                "প্রতি গর্তে প্রায় ১ চা চামচ (৫-৭ গ্রাম) মিশ্র সার দিয়ে মাটি দিয়ে ঢেকে দিন।",
                "সার দেওয়ার পরপরই হালকা সেচ দিন, যেন শিকড় পুড়ে না যায়।"
            ),
            tip = "চারা লাগানোর ২০-২৫ দিন পর এবং ফুল আসার আগে এভাবে পকেট সার দিলে গাছ দ্রুত ঝোপালো হয়।",
            orderIndex = 1L
        ),
        FeedingRecipe(
            id = "default_mustard_cake",
            title = "সরিষার খৈল পচিয়ে তরল সার তৈরি ও প্রয়োগ",
            category = "তরল সার",
            summary = "সরিষার খৈল পচানো তরল সার স্ট্রবেরির শিকড় মজবুত করে, পাতা সবুজ রাখে এবং প্রচুর ফুল-ফল আসতে সাহায্য করে।",
            dosage = "১ কেজি খৈল + ১০ লিটার পানি (প্রয়োগের সময় আরও ১০ গুণ পানি মেশাবেন)",
            steps = listOf(
                "একটি প্লাস্টিক ড্রাম বা বালতিতে ১ কেজি সরিষার খৈল ও ১০ লিটার পরিষ্কার পানি নিন।",
                "মুখ ঢেকে ছায়াযুক্ত স্থানে ৫ থেকে ৭ দিন পচান। প্রতিদিন একবার কাঠি দিয়ে নেড়ে দিন।",
                "ভালোভাবে পচে গেলে ও ফেনা কমে গেলে পাতলা কাপড় বা ছাঁকনি দিয়ে ছেঁকে নিন।",
                "১ লিটার পচানো খৈলের সাথে ১০ লিটার পরিষ্কার পানি মিশিয়ে পাতলা করে প্রতি গাছের গোড়ায় ১০০-১৫০ মিলি করে ঢেলে দিন।"
            ),
            tip = "সকাল বা বিকেলে রোদ কম থাকলে খৈল পচা পানি দিন। কখনোই ঘন অবস্থায় সরাসরি গোড়ায় দেবেন না।",
            orderIndex = 2L
        ),
        FeedingRecipe(
            id = "default_horlicks_spray",
            title = "ফল বড় ও মিষ্টি করার বিশেষ স্প্রে (হরলিক্স, গুড় ও অনুখাদ্য)",
            category = "বিশেষ স্প্রে",
            summary = "ফল ছোট থাকা অবস্থায় ও রঙ ধরার আগে এই বিশেষ অনুখাদ্য ও হরলিক্স-গুড় মিশ্রণ স্প্রে করলে স্ট্রবেরি বড়, রসালো ও বেশি মিষ্টি হয়।",
            dosage = "১৬ লিটার স্প্রে ট্যাংকে: ৫০ গ্রাম হরলিক্স + ১০০ গ্রাম গুড় + ১৫ গ্রাম বোরন/অনুখাদ্য",
            steps = listOf(
                "প্রথমে ১ লিটার হালকা কুসুম গরম পানিতে ৫০ গ্রাম হরলিক্স ও ১০০ গ্রাম খেজুর বা আখের গুড় ভালোভাবে গুলে নিন।",
                "এর সাথে চিলেটেড জিংক ও বোরন মিশ্রিত অনুখাদ্য ১৫ গ্রাম মিশিয়ে ছেঁকে নিন।",
                "পুরো মিশ্রণটি ১৬ লিটারের স্প্রে মেশিনে ঢেলে পরিষ্কার পানি দিয়ে ট্যাংক ভরে নিন।",
                "বিকেলের দিকে গাছের পাতা ও কচি ফলের ওপর ভালোভাবে স্প্রে করে দিন (১০-১২ দিন পরপর)।"
            ),
            tip = "ফুল ফোটার একদম মাঝামাঝি সময়ে স্প্রে করবেন না; ফুল ঝরে ফল ধরার পর স্প্রে করলে সেরা ফল পাওয়া যায়।",
            orderIndex = 3L
        )
    )
}

object DefaultFarmArticles {
    val categories = listOf("সার প্রয়োগ", "রোগবালাই দমন", "ফল বড় করার টিপস", "জমি ও চারা পরিচর্যা")

    val items: List<FarmArticle> = listOf(
        FarmArticle(
            id = "article_fruit_size",
            title = "স্ট্রবেরি ফল বড়, উজ্জ্বল ও বেশি মিষ্টি করার সহজ উপায়",
            category = "ফল বড় করার টিপস",
            summary = "ফুল থেকে কচি ফল ধরার পর পটাশ ও বোরন সারের পাশাপাশি অনুখাদ্য ও জৈব পুষ্টি কীভাবে কাজ করে তা জানুন।",
            body = "১) ফল ধরার শুরুতেই গাছ প্রতি ৫ গ্রাম এমওপি (পটাশ) ও সামান্য বোরন মিশিয়ে মাটিতে প্রয়োগ করলে ফলের আকার দ্রুত বড় হয়।\n\n২) প্রতি ১০-১২ দিন পরপর বিকেল বেলায় ১৬ লিটার পানিতে ৫০ গ্রাম হরলিক্স, ১০০ গ্রাম গুড় ও ১৫ গ্রাম চিলেটেড অনুখাদ্য ভালোভাবে গুলে স্প্রে করুন।\n\n৩) মাটিতে কালো বা সিলভার মালচিং পেপার ব্যবহার করলে ফল মাটিতে লেগে পচে না এবং ফলের রঙ টুকটুকে লাল হয়।\n\n৪) গাছে অতিরিক্ত রানার (লতা) বের হলে তা কাঁচি দিয়ে কেটে ফেলুন, তাহলে সব শক্তি সরাসরি ফলে যাবে।",
            publishedDateLabel = "কৃষি পরামর্শ",
            orderIndex = 1L
        ),
        FarmArticle(
            id = "article_disease_control",
            title = "পাতার দাগ ও ফল পচা রোগ দমনে করণীয়",
            category = "রোগবালাই দমন",
            summary = "শীত ও কুয়াশায় স্ট্রবেরির পাতায় দাগ ও গোড়া পচা দেখা দিলে দ্রুত সঠিক ব্যবস্থা নিন।",
            body = "১) সকালের দিকে পাতায় শিশির জমে থাকলে পাতায় লালচে বা বাদামী দাগ দেখা দিতে পারে। আক্রান্ত পুরোনো পাতা জমি থেকে তুলে সরিয়ে ফেলুন।\n\n২) ছত্রাকনাশক হিসেবে প্রতি লিটার পানিতে ২ গ্রাম ম্যানকোজেব অথবা ১ গ্রাম কার্বেন্ডাজিম মিশিয়ে ৭-১০ দিন পরপর স্প্রে করুন।\n\n৩) জমিতে কখনোই পানি জমতে দেবেন না। সেচের পর অতিরিক্ত পানি নিকাশের নালা পরিষ্কার রাখুন।",
            publishedDateLabel = "রোগ দমন",
            orderIndex = 2L
        ),
        FarmArticle(
            id = "article_soil_mulching",
            title = "চারা রোপণের পর মাটির আর্দ্রতা ও নিড়ানি ব্যবস্থাপনা",
            category = "জমি ও চারা পরিচর্যা",
            summary = "সুস্থ গাছের জন্য আগাছা দমন ও পরিমিত সেচ খুবই জরুরি।",
            body = "১) চারা লাগানোর ১৫ দিন পর থেকে নিয়মিত আগাছা বা নিড়ানি পরিষ্কার করুন, এতে মাটির পুষ্টি আগাছা খেতে পারবে না।\n\n২) দুপুরের কড়া রোদে সেচ দেবেন না; ভোর বেলা বা বিকেল ৪টার পর হালকা সেচ দেওয়া সবচেয়ে ভালো।\n\n৩) ৩ থেকে ৪ দিন পরপর মাটির জো দেখে সেচ দিন এবং এই অ্যাপে পরবর্তী সেচের তারিখ লিখে রাখুন।",
            publishedDateLabel = "বাগান পরিচর্যা",
            orderIndex = 3L
        )
    )
}
