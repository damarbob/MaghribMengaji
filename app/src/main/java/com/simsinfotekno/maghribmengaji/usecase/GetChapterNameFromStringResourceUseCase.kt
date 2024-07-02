package com.simsinfotekno.maghribmengaji.usecase

import android.content.Context

class GetChapterNameFromStringResourceUseCase {

    operator fun invoke(id: Int, context: Context): String {
        // Construct the resource name using the id
        val resourceName = "chapter_$id"

        // Get the resource ID from the context
        val resourceId = context.resources.getIdentifier(resourceName, "string", context.packageName)

        // Return the string value if the resource ID is valid
        return if (resourceId != 0) {
            context.getString(resourceId)
        } else {
            "Chapter not found" // Fallback if the resource ID is invalid
        }
    }
}