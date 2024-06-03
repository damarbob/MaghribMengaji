package com.simsinfotekno.maghribmengaji.event

import com.simsinfotekno.maghribmengaji.model.QuranPageStudent


class OnPageStudentRepositoryUpdate : OnRepositoryUpdate<QuranPageStudent?> {

    var pageStudent: QuranPageStudent? = null

    constructor(event: Event) : super(event)

    constructor(event: Event, pageStudent: QuranPageStudent?) : super(event) {
        this.pageStudent = pageStudent
    }

}

