package com.example.tickets_2

import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.tickets_2.models.Event
import com.example.tickets_2.models.ResponseData
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity() {


    private lateinit var spectacleButton: Button
    private lateinit var musicButton: Button
    private lateinit var circusButton: Button
    private lateinit var concertSpinner: Spinner
    private val status = "insales"
    private var date = getCurrentDate()
    private var eventId: Int = 1003
    private var currentDate = getCurrentDate()
    private var isImageExpanded = false

    private lateinit var eventInfoTextView: TextView // Объявление переменной для TextView
    private lateinit var eventImageView: ImageView // Объявление переменной для TextView

    private lateinit var costMin : TextView
    private lateinit var costMax: TextView
    private lateinit var submitButton: Button
    private lateinit var datePicker: DatePicker





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        // Находим TextView по его ID
        eventInfoTextView = findViewById(R.id.eventInfoTextView)
        eventImageView = findViewById(R.id.eventImageView)
        costMax=findViewById(R.id.maxPriceEditText)
        costMin=findViewById(R.id.minPriceEditText)


        spectacleButton = findViewById(R.id.spectacleButton)
        musicButton = findViewById(R.id.musicButton)
        circusButton = findViewById(R.id.circusButton)
        concertSpinner = findViewById(R.id.concert_spinner)
        submitButton = findViewById(R.id.submitButton)
        datePicker = findViewById(R.id.datePicker)


        // Устанавливаем слушателя для выбора даты
        datePicker.init(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth
        ) { view, year, monthOfYear, dayOfMonth ->
            // Получаем текущую дату
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Если выбранная дата меньше текущей даты, устанавливаем текущую дату
            if (year < currentYear || (year == currentYear && monthOfYear < currentMonth) || (year == currentYear && monthOfYear == currentMonth && dayOfMonth < currentDayOfMonth)) {
                datePicker.updateDate(currentYear, currentMonth, currentDayOfMonth)
                date = "$currentDayOfMonth.${currentMonth + 1}.$currentYear"
            } else {
                date = "$dayOfMonth.${monthOfYear + 1}.$year"
            }
            // Перезапускаем процесс получения данных с сервера
            fetchDataFromServer()
        }


        // Установим слушатели нажатий для каждой кнопки
        spectacleButton.setOnClickListener { updateEventId(1003); zoomView(it)} // Театр
        musicButton.setOnClickListener { updateEventId(1002);zoomView(it) } // Музыка
        circusButton.setOnClickListener {  updateEventId(1088);zoomView(it) } // Цирк



        // Загрузим данные по умолчанию (театры) при запуске приложения
        fetchDataFromServer()


        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                fetchDataFromServer()
                showNotification("Данные обновились")
            }
        }, 0, 30* 60 * 1000) // 30 минут в миллисекундах

    }



    private fun zoomView(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.5f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.5f)
        scaleX.duration = 300
        scaleY.duration = 300
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()
        scaleX.repeatCount = 1
        scaleY.repeatCount = 1
        scaleX.repeatMode = ObjectAnimator.REVERSE
        scaleY.repeatMode = ObjectAnimator.REVERSE
        scaleX.start()
        scaleY.start()
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // January is 0
        val year = calendar.get(Calendar.YEAR)
        return "$day.$month.$year"
    }

    private fun updateEventId(id: Int) {
        eventId = id
        // После обновления ID загрузим данные с новым ID
        fetchDataFromServer()

    }


    private fun fetchDataFromServer() {
        // Создаем клиент OkHttp
        val client = OkHttpClient()

        // Создаем запрос
        val url = "https://www.kvitki.by/ajaxCaller/method:getConcertsListInfo/id:$eventId/type:catalog_category/status:$status/date:$date/order:date,asc/page:1/design:kvitki/portal:2/?fields%5Bevent%5D=id,title,promoterId,minPrice,decoratedTitle,discount,shortUrl,shortImageUrl,specialStatus,venueDescription,salesStatus,localisedStartDate,localisedEndDate,startTime,endTime,centerId,prices,buyButtonConfig,badgeData,type,customTargetUrl,subMode,decoratedShortContent,salestart,countryCode"
        val request = Request.Builder()
            .url(url)
            .build()

        // Запускаем запрос асинхронно с использованием корутин
        GlobalScope.launch(Dispatchers.IO) {
            // Отправляем запрос и получаем ответ
            val response = client.newCall(request).execute()

            // Обрабатываем ответ в основном потоке
            launch(Dispatchers.Main) {
                handleResponse(response)
            }
        }



    }
    private fun handleResponse(response: Response) {
        // Проверяем, успешен ли запрос
        if (response.isSuccessful) {
            // Получаем тело ответа
            val responseBody = response.body?.string()
            if (responseBody != null) {
                Log.d("Response Body", responseBody)
                try {
                    // Парсим JSON-ответ в объект EventListResponse
                    val eventListResponse = Gson().fromJson(responseBody, ResponseData::class.java)
                    // Создаем экземпляр Gson


                    // Показываем в логах, что было распарсено из JSON
                    Log.d("ResponseData", eventListResponse.toString())

                    // Получаем список событий
                    val events = eventListResponse.responseData.event
                    val listInfos = eventListResponse.responseData.listInfo

                    // Получаем ссылку на спиннер по его ID
                    val spinner: Spinner = findViewById(R.id.concert_spinner)

                    // Проверяем, что список событий не равен null и не пуст
                    if (!events.isNullOrEmpty()) {
                        // Выводим список названий концертов
                        val concertNames = events.map { it.title }
                        val adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_spinner_item, concertNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter // Устанавливаем адаптер для спиннера

                        // Выводим названия событий в лог
                        events.forEach { event ->
                            Log.d("Event Title", event.title)
                            // Здесь можно добавить вывод других свойств события, если нужно

                        }

                        // Установка слушателя для выбора элемента в Spinner
                        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                                // Получите выбранный элемент из Spinner
                                val selectedEvent = events[position]
                                val selectedInfos = listInfos

                                // Форматирование информации о выбранном событии
                                val eventInfo = "Цена: ${selectedEvent.prices} BYN \nВремя начала: ${selectedEvent.startTime.time}\nАдрес: ${selectedEvent.venueDescription} "

                                // Установите текст в TextView
                                eventInfoTextView.text = eventInfo
                                val eventImg = selectedEvent.shortImageUrl
                                // Установите текст в TextView
                                Picasso.get().load(eventImg).into(eventImageView)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // Обработка события, когда ничего не выбрано
                                eventInfoTextView.text = ""
                            }
                        }


                        // Проверяем наличие новой даты
                        val newDate = getNewDate(events)
                        if (newDate.isNotEmpty() && newDate > currentDate) {
                            // Если обнаружена новая дата, показываем уведомление
                            showNotification("Найдена новая дата: $newDate")
                            // Обновляем текущую дату
                            currentDate = newDate
                        }



                        submitButton.setOnClickListener {
                            zoomView(it)
                            // Получаем введенные пользователем значения минимальной и максимальной цены
                            val minPriceStr = costMin.text.toString() // Получаем строковое значение из TextView и преобразуем его в строку
                            val maxPriceStr = costMax.text.toString() // Получаем строковое значение из TextView и преобразуем его в строку


                            // Преобразуем строковые значения в числа
                            val minPrice = minPriceStr.toDoubleOrNull()
                            val maxPrice = maxPriceStr.toDoubleOrNull()

                            // Проверяем, получилось ли преобразовать введенные значения в числа
                            if (minPrice != null && maxPrice != null) {
                                // Проверяем, что минимальная цена меньше или равна максимальной цене
                                if (minPrice <= maxPrice) {

                                    // Получаем выбранное событие из спиннера
                                    val selectedEventPosition = spinner.selectedItemPosition
                                    val selectedEvent = events[selectedEventPosition]

                                    // Проверяем, попадает ли цена выбранного события в диапазон, указанный пользователем
                                    if (selectedEvent.minPrice.toDouble() in minPrice..maxPrice) {
                                        // Если да, показываем уведомление
                                        showNotification("${selectedEvent.title} попадает в диапазон цен от $minPrice до $maxPrice")
                                    } else {
                                        // Если нет, выводим сообщение о том, что выбранное событие не попадает в указанный диапазон цен
                                        showNotification("${selectedEvent.title} не попадает в диапазон цен от $minPrice до $maxPrice")
                                    }
                                } else {
                                    // Если минимальное значение больше максимального, очищаем минимальное поле
                                    costMax.text = null
                                }
                            }  else {
                                // Если не удалось преобразовать введенные значения в числа, показываем сообщение об ошибке
                                showNotification("Введите корректные числовые значения для минимальной и максимальной цены")
                            }
                        }


                    } else {
                    // Список событий пустой
                    Log.d("No concerts available", "")
                }
                } catch (e: Exception) {
                    // Обработка ошибок парсинга JSON
                    Log.e("JSON parsing error", e.message ?: "Unknown error")
                }
            } else {
                // Обработка случая, когда тело ответа пустое
                Log.d("Empty response body", "")
            }
        } else {
            // Если запрос не успешен, отображаем уведомление об ошибке
            showNotification("Error")
        }



    }



    private fun getNewDate(events: List<Event>): String {
        var newDate = ""
        for (event in events) {
            // Получаем дату события из JSON-ответа (примерно)
            val eventDate = event.startTime.shortDate // Предположим, что в объекте Event есть поле startDate

            // Проверяем, если дата события больше текущей даты и даты, которую уже ранее обнаружили
            if (eventDate.isNotEmpty() && eventDate > currentDate && (newDate.isEmpty() || eventDate > newDate)) {
                newDate = eventDate
            }
        }
        return newDate
    }


//    private fun updateUI(events: List<Event>) {
//        // Обновление пользовательского интерфейса событиями
//        // Здесь вы можете обновить ваш пользовательский интерфейс с помощью списка событий
//    }


    private fun showNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Создаем канал уведомлений (требуется для API 26+)
            val channelId = "default_channel_id"
            val channelName = "Default Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
        }
//        // Создаем интент для запуска вашей активити
//        val intent = Intent(this, MainActivity::class.java)
//        // Устанавливаем флаги для интента, чтобы он запускал вашу активити, даже если приложение находится в фоновом режиме
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val notificationBuilder = NotificationCompat.Builder(this, "default_channel_id")
            .setContentTitle("Уведомление")
            .setContentText(message)
            .setSmallIcon(R.drawable.baseline_add_alert_24) //  иконкa уведомления
            .setAutoCancel(true)
            //.setContentIntent(pendingIntent) // Устанавливаем интент для запуска активити по нажатию на уведомление

        val notification = notificationBuilder.build()
        notificationManager.notify(0, notification)
    }

}


