package text.foryou.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import text.foryou.NEW_FONT_ID
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "fontscolor")
class FontColorEntity (
        @PrimaryKey(autoGenerate = true)
        var id: Int,
        var color: String, //gradient or video
        var name: String

) : Parcelable {
    constructor(): this(NEW_FONT_ID,"", "")
    constructor(color:String, name:String): this(NEW_FONT_ID,color, name)

}