package com.simsinfotekno.maghribmengaji.event

import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiStudent

class OnUserDataLoaded(val maghribMengajiStudent: MaghribMengajiStudent, val userDataEvent: UserDataEvent) {
}