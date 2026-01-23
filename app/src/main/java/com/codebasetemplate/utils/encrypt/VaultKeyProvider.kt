import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import com.codebasetemplate.utils.encrypt.KeystoreManager
import javax.crypto.SecretKey

object VaultKeyProvider {

    private const val PREF_NAME = "vault_secure"
    private const val PREF_MASTER_KEY = "encrypted_master_key"

    @Volatile
    private var cachedMasterKey: SecretKey? = null

    fun getMasterKey(context: Context): SecretKey {
        cachedMasterKey?.let { return it }

        synchronized(this) {
            cachedMasterKey?.let { return it }

            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val kek = KeystoreManager.getOrCreateKey()

            val encryptedBase64 = prefs.getString(PREF_MASTER_KEY, null)

            val masterKey = if (encryptedBase64 == null) {
                val newKey = KeystoreManager.generateMasterKey()
                val encrypted = KeystoreManager.encryptMasterKey(newKey, kek)

                prefs.edit {
                    putString(
                        PREF_MASTER_KEY,
                        Base64.encodeToString(encrypted, Base64.NO_WRAP)
                    )
                }

                newKey
            } else {
                val encrypted = Base64.decode(encryptedBase64, Base64.NO_WRAP)
                KeystoreManager.decryptMasterKey(encrypted, kek)
            }

            cachedMasterKey = masterKey
            return masterKey
        }
    }

    fun clearMemoryCache() {
        cachedMasterKey = null
    }
}