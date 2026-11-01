package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromEducationLevel(value: EducationLevel?): String? = value?.name

    @TypeConverter
    fun toEducationLevel(value: String?): EducationLevel? = value?.let { EducationLevel.valueOf(it) }

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromEducationEntryList(value: List<EducationEntry>): String = Json.encodeToString(value)

    @TypeConverter
    fun toEducationEntryList(value: String): List<EducationEntry> = Json.decodeFromString(value)

    @TypeConverter
    fun fromExperienceEntryList(value: List<ExperienceEntry>): String = Json.encodeToString(value)

    @TypeConverter
    fun toExperienceEntryList(value: String): List<ExperienceEntry> = Json.decodeFromString(value)

    @TypeConverter
    fun fromCertificateEntryList(value: List<CertificateEntry>): String = Json.encodeToString(value)

    @TypeConverter
    fun toCertificateEntryList(value: String): List<CertificateEntry> = Json.decodeFromString(value)

    @TypeConverter
    fun fromXPTransactionType(value: XPTransactionType): String = value.name

    @TypeConverter
    fun toXPTransactionType(value: String): XPTransactionType = XPTransactionType.valueOf(value)

    @TypeConverter
    fun fromRewardRequestStatus(value: RewardRequestStatus): String = value.name

    @TypeConverter
    fun toRewardRequestStatus(value: String): RewardRequestStatus = RewardRequestStatus.valueOf(value)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)

    @TypeConverter
    fun fromSubjectResultList(value: List<SubjectResult>): String = Json.encodeToString(value)

    @TypeConverter
    fun toSubjectResultList(value: String): List<SubjectResult> = Json.decodeFromString(value)
}
