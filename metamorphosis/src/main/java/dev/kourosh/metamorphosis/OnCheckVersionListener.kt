package dev.kourosh.metamorphosis


interface OnCheckVersionListener {
    fun onSucceed(data:String)
    fun onFailed(message: String, code: Int?)
}
