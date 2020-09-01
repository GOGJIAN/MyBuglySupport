package com.shimao.mybuglylib.data


/**
 * @author jian
 *
 */
interface ICallBack<T> {
    fun onNext(data: T?)
    fun onDownloadFinish()
    fun onError(e: String)
    fun onUploadFinish()

    open class CallBackImpl<T> : ICallBack<T> {
        override fun onDownloadFinish() {

        }

        override fun onNext(data: T?) {

        }

        override fun onError(e: String) {

        }

        override fun onUploadFinish() {

        }
    }
}
