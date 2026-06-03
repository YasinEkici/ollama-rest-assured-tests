# Ollama API Regression Test Automation

## About

Bu proje, Yazılım Test Mühendisliği dersi için hazırlanmış bir otomatik API regresyon test projesidir. Java 17, Maven, JUnit 5 ve Rest Assured kullanarak lokal çalışan Ollama servisinin native API endpoint'leri test edilir.

Projenin odağı, LLM çıktılarının doğal değişkenliğini dikkate alan, çalıştırılabilir ve sunumda gösterilebilir bir test paketi oluşturmaktır. Bu nedenle testler model cevabının anlamını değil; HTTP durum kodu, JSON yapısı, alan varlığı, tipler ve cevap süresi gibi yapısal özellikleri doğrular.

## Architecture

Test paketi tek modüllü bir Maven projesidir. Bu projede ayrı bir üretim uygulaması yoktur; repository'nin kendisi API test suite olarak tasarlanmıştır. Ortak Rest Assured ayarları, endpoint sabitleri, konfigürasyon okuyucu ve request/response POJO sınıfları `src/test/java/com/example/ollamatests/` altında tutulur; endpoint testleri ise `tags`, `generate` ve `chat` paketlerinde ayrılır.

Daha ayrıntılı klasör ve sorumluluk açıklaması için [project_structure.md](project_structure.md) dosyasına bakılabilir. Not: Mevcut uygulamada helper ve model sınıfları test scope içinde tutulur; bu tercih Rest Assured bağımlılığının test scope kalması için yapılmıştır.

Temel yapı:

```text
src/test/java/com/example/ollamatests/
├── BaseTest.java
├── client/
│   ├── Endpoints.java
│   └── OllamaSpecs.java
├── config/
│   └── TestConfig.java
├── model/
│   ├── chat/
│   ├── generate/
│   └── tags/
├── chat/
├── generate/
└── tags/
```

Test edilen endpoint'ler:

- `GET /api/tags`
- `POST /api/generate`
- `POST /api/chat`

## Requirements

Projeyi çalıştırmak için aşağıdaki araçlar gerekir:

- Java 17
- Maven 3.9 veya üzeri
- Ollama
- Lokal olarak indirilmiş `qwen2.5:0.5b` modeli

Ollama kurulumu için resmi site: <https://ollama.com>

Rest Assured dokümantasyonu: <https://rest-assured.io/>

## Setup

Önce Ollama servisinin çalıştığından ve test modelinin indirildiğinden emin olun:

```bash
# Ollama
ollama serve
ollama pull qwen2.5:0.5b
```

Ardından projeyi klonlayıp testleri çalıştırın:

```bash
# Project
git clone <repo-url>
cd ollama-api-tests
mvn clean test
```

Varsayılan konfigürasyon lokal Ollama adresini kullanır:

```text
http://localhost:11434
```

## Configuration

Varsayılan değerler `src/test/resources/test-config.properties` dosyasında tutulur:

```properties
ollama.base.url=http://localhost:11434
ollama.test.model=qwen2.5:0.5b
ollama.timeout.ms=60000
```

Konfigürasyon çözümleme sırası:

1. Sistem property
2. Environment variable
3. `test-config.properties`
4. Hardcoded default

Desteklenen environment variable değerleri:

```bash
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_TEST_MODEL=qwen2.5:0.5b
OLLAMA_TIMEOUT_MS=60000
```

Örnek:

```bash
OLLAMA_BASE_URL=http://192.168.1.50:11434 mvn test
```

Windows PowerShell örneği:

```powershell
$env:OLLAMA_BASE_URL="http://localhost:11434"
mvn test
```

## Running Tests

Tüm testleri çalıştırmak için:

```bash
mvn clean test
```

Smoke testleri çalıştırmak için:

```bash
mvn test -Dgroups=smoke
```

Regression testleri çalıştırmak için:

```bash
mvn test -Dgroups=regression
```

Tek bir test sınıfını çalıştırmak için:

```bash
mvn test -Dtest=GenerateApiTest
```

Mevcut test dağılımı:

- Smoke: 3 test, her endpoint için bir temel test
- Regression: 22 test, tüm pozitif ve negatif senaryolar
- Default `mvn test`: 22 test

Test sınıfları:

- `TagsApiTest`
- `GenerateApiTest`
- `GenerateNegativeTest`
- `ChatApiTest`
- `ChatNegativeTest`

## Test Strategy

LLM cevapları aynı prompt ile bile ortam, model sürümü ve çalışma zamanı koşullarına göre değişebilir. Bu nedenle testler semantik içerik doğrulaması yapmaz; örneğin model cevabının belirli bir cümleyi içerdiği varsayılmaz.

Kullanılan doğrulama tipleri:

- HTTP status code kontrolü
- JSON alanlarının varlığı ve tipi
- Response body içinde yapısal alan kontrolleri
- Sayısal eşik kontrolleri
- Cevap süresi kontrolleri
- POJO deserialize kontrolleri

`generate` ve `chat` isteklerinde değişkenliği azaltmak için her geçerli request aşağıdaki ayarlarla gönderilir:

```json
{
  "stream": false,
  "options": {
    "temperature": 0,
    "seed": 42
  }
}
```

Negatif testlerde HTTP status code ve hata body yapısı genel REST beklentilerine göre varsayılmamıştır. Önce Ollama'nın gerçek cevabı gözlemlenmiş, sonra assertion'lar bu gerçek davranışa göre sıkılaştırılmıştır.

## Known Limitations

- Testler canlı lokal Ollama servisine bağlıdır; servis çalışmıyorsa testler geçmez.
- `qwen2.5:0.5b` modeli lokal makinede indirilmiş olmalıdır.
- LLM çıktıları tamamen deterministik değildir; testler bu yüzden anlam doğrulaması yapmaz.
- Cevap süreleri makine donanımına, model yükleme durumuna ve Ollama çalışma koşullarına göre değişebilir.
- Streaming testleri kapsam dışıdır; tüm request'lerde `stream: false` kullanılır.
- OpenAI uyumlu `/v1/...` endpoint'leri ve embedding endpoint'leri bu proje kapsamında test edilmez.
