package com.simsinfotekno.maghribmengaji.event

import com.simsinfotekno.maghribmengaji.enums.UserDataEvent
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class OnUserDataLoaded(val maghribMengajiUser: MaghribMengajiUser, val userDataEvent: UserDataEvent) {
}