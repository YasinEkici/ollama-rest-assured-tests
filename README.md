# 🤖 Ollama API Regression Test Automation

![Java 17](https://img.shields.io/badge/Java-17-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36.svg)
![JUnit 5](https://img.shields.io/badge/JUnit-5.11.3-25A162.svg)
![Rest Assured](https://img.shields.io/badge/Rest_Assured-5.5.7-43B02A.svg)

Bu proje, **Yazılım Test Mühendisliği** dersi için hazırlanmış otomatik bir API regresyon test projesidir. Proje, lokalde çalışan Ollama servisinin native API uçlarını (endpoint) Java 17, Maven, JUnit 5 ve Rest Assured kullanarak test eder.

> 💡 **Sunum Dosyası:** Proje kapsamında hazırlanan *"Test Mühendisliğinde Etkili Promptlar"* sunumuna [`docs/presentation/YasinEkici_Test_Müh_Etkili_Promptlar.pdf`](docs/presentation/YasinEkici_Test_Müh_Etkili_Promptlar.pdf) yolundan ulaşabilirsiniz.

---

## 🎯 Proje Odağı ve Test Stratejisi

LLM (Büyük Dil Modeli) çıktılarının doğal değişkenliği (non-determinism) nedeniyle, bu projede geleneksel "anlamsal eşleşme" (semantic matching) yerine **yapısal test stratejisi** izlenmiştir.

Testler model cevabının "ne anlama geldiğini" değil;
- ✅ HTTP durum kodu doğruluklarını,
- ✅ JSON yapısını ve alan (field) varlıklarını,
- ✅ Veri tiplerini ve sayısal eşikleri,
- ✅ Cevap süresi (timeout) limitlerini kontrol eder.

*Daha stabil sonuçlar almak için isteklerde `stream: false`, `temperature: 0` ve `seed: 42` parametreleri kullanılır.*

---

## 🏗️ Mimari ve Dizin Yapısı

Bu repository, ayrı bir üretim (production) kodu barındırmayan, tamamen test odaklı tek modüllü bir Maven projesidir. Test sınıflarının karmaşıklığını azaltmak için ortak ayarlar, konfigürasyon okuyucular ve request/response POJO'ları yardımcı paketlere ayrılmıştır. Detaylı bilgi için [project_structure.md](project_structure.md) dosyasını inceleyebilirsiniz.

```text
src/test/java/com/example/ollamatests/
├── BaseTest.java          # Rest Assured ve ortam hazırlığı
├── client/                # Endpoint sabitleri ve Spec builder'lar
├── config/                # Environment ve property okuyucuları
├── model/                 # Request & Response POJO nesneleri
├── tags/                  # GET /api/tags testleri
├── generate/              # POST /api/generate testleri
└── chat/                  # POST /api/chat testleri
```

---

## ⚙️ Gereksinimler ve Kurulum

Projeyi kendi ortamınızda çalıştırmak için aşağıdaki araçların kurulu olması gerekir:

- **Java 17** veya üzeri
- **Maven 3.9** veya üzeri
- **Ollama** ([İndir](https://ollama.com))

### 1. Ollama ve Modelin Hazırlanması
Terminali açın ve `qwen2.5:0.5b` modelini indirip servisi başlatın:
```bash
ollama serve
ollama pull qwen2.5:0.5b
```

### 2. Projenin Çalıştırılması
```bash
git clone <repo-url>
cd ollama-api-tests
mvn clean test
```

---

## 🔧 Konfigürasyon

Test paketi, URL ve model bilgilerini dinamik olarak yönetir. Varsayılan konfigürasyon, sistemi `http://localhost:11434` adresinde arar.
Ayarlar `src/test/resources/test-config.properties` içindedir:

```properties
ollama.base.url=http://localhost:11434
ollama.test.model=qwen2.5:0.5b
ollama.timeout.ms=60000
```

Eğer uzak bir Ollama sunucusu kullanacaksanız, Ortam Değişkenleri (Environment Variables) ile bu ayarları ezebilirsiniz:
```bash
# Linux/macOS
OLLAMA_BASE_URL=http://192.168.1.50:11434 mvn clean test

# Windows PowerShell
$env:OLLAMA_BASE_URL="http://192.168.1.50:11434"
mvn clean test
```

---

## 🚀 Testleri Koşma (Maven Profilleri)

Projede testler JUnit 5 `@Tag` notasyonlarıyla `smoke` ve `regression` olarak ikiye ayrılmıştır. Toplam 22 test (3 smoke, 19 regression senaryosu) mevcuttur.

**Tüm Testleri Koşmak (Varsayılan):**
```bash
mvn clean test
```

**Sadece Smoke Testleri (Hızlı Kontrol):**
```bash
mvn test -Dgroups=smoke
```

**Sadece Regression Testleri (Derinlemesine Kontrol):**
```bash
mvn test -Dgroups=regression
```

**Belirli Bir Test Sınıfını Koşmak:**
```bash
mvn test -Dtest=GenerateApiTest
```

---

## ⚠️ Bilinen Kısıtlamalar (Limitations)

- **Canlı Servis Bağımlılığı:** Testler mock (sahte) verilere değil, canlı Ollama servisine atılır. Servis kapalıysa testler fail olur.
- **Model Bağımlılığı:** Gecikmeleri (latency) düşük tutmak adına küçük bir model olan `qwen2.5:0.5b` hedeflenmiştir. Farklı model kullanmak isterseniz property dosyasını güncellemelisiniz.
- **Kapsam:** Streaming asenkron okuma gerektirdiği için kapsam dışıdır. Ayrıca OpenAI uyumlu endpoint'ler (`/v1/`) projeye dahil edilmemiştir.
