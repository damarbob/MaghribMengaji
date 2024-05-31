package com.simsinfotekno.maghribmengaji.event


abstract class OnRepositoryUpdate<T>(open val status: Event) {
    enum class Event {
        RECORD_ADDED,
        RECORD_DELETED,
        RECORDS_CLEARED,
        ACTION_CREATE,
        ACTION_ADD,
        ACTION_DELETE,
        ACTION_CLEAR,
        ACTION_NONE,
        ACTION_SET_DEFAULT,
        ACTION_CLEAR_DEFAULT
    }

}

