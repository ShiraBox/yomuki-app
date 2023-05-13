package me.urfate.yomuki.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import me.urfate.yomuki.source.ContentSource

@Entity
class SourceEntity {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @JvmField
    @ColumnInfo(name = "name")
    var name: String

    @JvmField
    @ColumnInfo(name = "url")
    var url: String

    @ColumnInfo(name = "icon_url")
    var iconUrl: String

    @JvmField
    @ColumnInfo(name = "is_default")
    var isDefault: Int

    constructor(id: Int, name: String, url: String, iconUrl: String, isDefault: Int) {
        this.id = id
        this.name = name
        this.url = url
        this.iconUrl = iconUrl
        this.isDefault = isDefault
    }

    @Ignore
    constructor(source: ContentSource, isDefault: Boolean) {
        name = source.name
        url = source.url
        iconUrl = source.iconUrl
        this.isDefault = if (isDefault) 1 else 0
    }
}