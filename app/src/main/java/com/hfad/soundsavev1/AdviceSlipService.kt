import com.hfad.soundsavev1.AdviceSlipResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Define AdviceSlipService as an interface
interface AdviceSlipService {
    @GET("advice")
    fun getAdvice(): Call<AdviceSlipResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.adviceslip.com/"
    val adviceSlipService: AdviceSlipService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdviceSlipService::class.java)
    }
}