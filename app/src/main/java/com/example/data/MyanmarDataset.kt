package com.example.data

data class MyanmarDataset(
    val name: String,
    val category: String,
    val contentType: String,
    val format: String,
    val size: String,
    val sourceUrl: String,
    val description: String,
    val sampleFormat: String,
    val sampleData: String,
    val pythonCode: String,
    val kotlinCode: String
)

object MyanmarDatasetProvider {
    val datasets = listOf(
        MyanmarDataset(
            name = "CC100-Burmese Monolingual Dataset",
            category = "Monolingual",
            contentType = "General Monolingual Plain Text",
            format = "TXT / Parquet",
            size = "~993 MB (13.7M rows)",
            sourceUrl = "https://huggingface.co/datasets/chuuhtetnaing/myanmar-cc100-dataset",
            description = "မြန်မာစာ Unicode monolingual (သာမန်စာသား) စုစည်းမှုဖြစ်ပြီး၊ CommonCrawl မှ သန့်စင်ထားသည်။ data cleaning, word segmentation, sentence splitting, and unsupervised pre-training လုပ်ငန်းများအတွက် အထူးသင့်လျော်သည်။",
            sampleFormat = "CSV",
            sampleData = "\"text\"\n\"ဘူးသေးပါ။ သွားရည်ကျသွားပါတယ်။\"\n\"ဆောင်းတွင်း မရှိဘူး။\"\n\"‘ဟဲ့ မေမိုး အစားနည်းလှချေလား နေရောကောင်းရဲ့’\"\n\"ဘဝမှတ်စုများ\"\n\"ထက်သန်-လွန်ကဲ-သော တွေဝေခြင်း\"\n\"'မောဟ' သဘောရှိသော၊ မောဟကြီးသော သဘောရှိသော၊ သူ။\"",
            pythonCode = "# HuggingFace Python code to load CC100-Burmese\nfrom datasets import load_dataset\n\ndataset = load_dataset(\"chuuhtetnaing/myanmar-cc100-dataset\", split=\"train\")\nprint(f\"Total rows: {len(dataset)}\")\nprint(dataset[0])",
            kotlinCode = "// Kotlin Room entity to hold monolingual data\n@Entity(tableName = \"cc100_burmese\")\ndata class Cc100Burmese(\n    @PrimaryKey(autoGenerate = true) val id: Long = 0,\n    val text: String\n)"
        ),
        MyanmarDataset(
            name = "Myanmar Written Corpus Dataset",
            category = "Monolingual",
            contentType = "Monolingual Written Text with metadata",
            format = "Parquet / TXT",
            size = "~10.7M rows",
            sourceUrl = "https://huggingface.co/datasets/freococo/myanmar-written-corpus",
            description = "မြန်မာစာ monolingual writing/use case စာသားများ ဖြစ်သည်။ sentence_id, text, string_length, length_category (<100, <200, <300, <400) တို့ပါဝင်သည်။ AI text generation, classification, and TTS/ASR validation တို့အတွက် ကောင်းမွန်သည်။",
            sampleFormat = "JSON",
            sampleData = "[\n  {\n    \"sentence_id\": 10423,\n    \"text\": \"ထောက်လှမ်းရေးအေဂျင်စီ S.H.I.E.L.D၏ ညွှန်ကြားရေးမှူး နစ်ဖြူရီသည်...\",\n    \"string_length\": 78,\n    \"length_category\": \"<100\"\n  },\n  {\n    \"sentence_id\": 10424,\n    \"text\": \"လိုကီသည် ထွက်ပြေးသည့်အတွက် ဝန်ထမ်းများ စိုးရိမ်ခဲ့ကြသည်...\",\n    \"string_length\": 60,\n    \"length_category\": \"<100\"\n  }\n]",
            pythonCode = "# Load Myanmar Written Corpus from HuggingFace\nimport pandas as pd\n\ndf = pd.read_parquet(\"hf://datasets/freococo/myanmar-written-corpus/data/train-00000-of-00001.parquet\")\nprint(df.head())",
            kotlinCode = "// Model definition for Written Corpus\ndata class WrittenSentence(\n    val sentenceId: Long,\n    val text: String,\n    val stringLength: Int,\n    val lengthCategory: String\n)"
        ),
        MyanmarDataset(
            name = "Burmese Language Corpus (BurmeseCorpus)",
            category = "Monolingual",
            contentType = "Monolingual Plain Text",
            format = "TXT",
            size = "> 150,000 sentences per file",
            sourceUrl = "https://github.com/1chimaruGin/BurmeseCorpus",
            description = "CommonCrawl-Unicode Burmese datasets ဖြစ်သည်။ NLP pre-training, word embeddings (Word2Vec/FastText), text cleaning pipeline စမ်းသပ်မှုများအတွက် အသုံးပြုနိုင်သည်။",
            sampleFormat = "TXT",
            sampleData = "ဘူးသေးပါ။\nသွားရည်ကျသွားပါတယ်။\nဆောင်းတွင်း မရှိဘူး။\nမင်္ဂလာပါခင်ဗျာ။",
            pythonCode = "# Reading plain text file\nwith open(\"BurmeseCorpus.txt\", \"r\", encoding=\"utf-8\") as f:\n    lines = f.readlines()\n    for line in lines[:5]:\n        print(line.strip())",
            kotlinCode = "// File reader helper in Kotlin\nfun readCorpusFile(context: Context, resId: Int): List<String> {\n    return context.resources.openRawResource(resId).bufferedReader().readLines()\n}"
        ),
        MyanmarDataset(
            name = "SEAlang Burmese Text Corpus",
            category = "Monolingual",
            contentType = "Online Dictionary & Syntax Corpus",
            format = "TXT (via search api)",
            size = "50M+ characters",
            sourceUrl = "http://sealang.net/burmese/corpus.htm",
            description = "Online corpus search & monolingual corpus ဖြစ်သည်။ collocates, dictionary/search enrichment, syntax and grammatical analysis, and tokenization research အတွက် သုတေသနအခြေပြု အသုံးပြုနိုင်သည်။",
            sampleFormat = "TXT",
            sampleData = "မြန်မာစာပေ၏ အရင်းအမြစ်များကို ရှာဖွေလေ့လာနိုင်သော စာသားစုစည်းမှု။",
            pythonCode = "# Fetch search collocates from SEAlang API (Simulation)\nimport requests\n\nresponse = requests.get(\"http://sealang.net/burmese/search?query=မြန်မာ\")\nprint(response.status_code)",
            kotlinCode = "// Retrofit client to request search tokens\ninterface SeaLangService {\n    @GET(\"search\")\n    suspend fun searchCorpus(@Query(\"query\") query: String): ResponseBody\n}"
        ),
        MyanmarDataset(
            name = "Myanmar Wikipedia Dump",
            category = "Monolingual",
            contentType = "Factual Encyclopedic Text",
            format = "XML / JSONL / TXT",
            size = "Compressed ~19GB, Decompressed ~86GB",
            sourceUrl = "https://dumps.wikimedia.org/mywiki/",
            description = "မြန်မာ Wikipedia ဆောင်းပါးများအားလုံး၏ စုစည်းမှုဖြစ်သည်။ Knowledge-based Q&A, summarization, document retrieval, and factual/encyclopedic LLM evaluation benchmark များအတွက် အရေးပါသော corpus ဖြစ်သည်။",
            sampleFormat = "XML",
            sampleData = "<page>\n  <title>မြန်မာနိုင်ငံ</title>\n  <id>201</id>\n  <revision>\n    <text>မြန်မာနိုင်ငံသည် အာရှတောင်ပိုင်းဒေသမှ တိုင်းပြည်တစ်နိုင်ငံဖြစ်သည်။ မြောက်ဘက်တွင် တရုတ်နိုင်ငံ၊ အရှေ့ဘက်တွင် လာအိုနှင့် ထိုင်းနိုင်ငံတို့ တည်ရှိသည်။</text>\n  </revision>\n</page>",
            pythonCode = "# Parse Wikipedia XML dump with Python\nimport xml.etree.ElementTree as ET\n\ntree = ET.parse(\"mywiki-latest-pages-articles.xml\")\nroot = tree.getroot()\nfor page in root.findall(\".//page\")[:3]:\n    title = page.find(\"title\").text\n    print(f\"Title: {title}\")",
            kotlinCode = "// XML pull parser for wiki dump locally\nfun parseWikiXml(inputStream: InputStream) {\n    val parser = Xml.newPullParser()\n    parser.setInput(inputStream, \"UTF-8\")\n    // Parse loop...\n}"
        ),
        MyanmarDataset(
            name = "MM-Lib Book Corpus Dataset",
            category = "Monolingual",
            contentType = "Literature & Novels Full Text",
            format = "TXT / Parquet",
            size = "153MB (437 Books)",
            sourceUrl = "https://huggingface.co/datasets/chuuhtetnaing/mm-lib-book-dataset",
            description = "မြန်မာစာအုပ်များ၏ စာကြောင်းပြည့် စာသားစုစည်းမှုဖြစ်သည်။ Long-context generation, reasoning, story understanding, and large-context LLM benchmark များအတွက် သင့်လျော်သည်။",
            sampleFormat = "JSON",
            sampleData = "{\n  \"title\": \"ပြစ်မှု နှင့် ပြစ်ဒဏ်\",\n  \"author_name\": \"တင်မောင်မြင့် (ဘာသာပြန်)\",\n  \"category\": \"ဘာသာပြန် ဝတ္ထု\",\n  \"raw_text\": \"ပြစ်မှု နှင့် ပြစ်ဒဏ်။ လူသားတို့၏ စိတ်ပိုင်းဆိုင်ရာ ပဋိပက္ခများကို ဖော်ထုတ်ထားသည့် ကမ္ဘာကျော် ဂန္ထဝင်စာအုပ်ကြီး ဖြစ်သည်။\"\n}",
            pythonCode = "# Load MM-Lib Book Corpus from HuggingFace\nfrom datasets import load_dataset\n\ndataset = load_dataset(\"chuuhtetnaing/mm-lib-book-dataset\")\nbook = dataset[\"train\"][0]\nprint(f\"Title: {book['title']}\")\nprint(f\"First 100 chars: {book['raw_text'][:100]}\")",
            kotlinCode = "// Read full book content locally from database\ndata class Book(\n    val title: String,\n    val authorName: String,\n    val category: String,\n    val rawText: String\n)"
        ),
        MyanmarDataset(
            name = "mmC4 Burmese Dataset",
            category = "Web Crawl / Cleaned",
            contentType = "Cleaned Web Content",
            format = "JSONL",
            size = "Burmese portion of mC4",
            sourceUrl = "https://huggingface.co/datasets/allenai/c4",
            description = "Multimodal Cleaned Crawl Corpus မှ မြန်မာဘာသာစကား သီးသန့် စာသားအပိုင်းဖြစ်သည်။ Encoding ပြဿနာများ ပြုပြင်ပြီး၊ PII (Personal Identifiable Information) များ ဖယ်ရှားထားသည်။",
            sampleFormat = "JSON",
            sampleData = "{\n  \"text\": \"မြန်မာစာအကြောင်းအရာဖြစ်သည်။ Encoding ပြဿနာများစနစ်တကျ ပြုပြင်ပြီး၊ PII များဖယ်ရှားထားသည်။\",\n  \"language\": \"my_MM\"\n}",
            pythonCode = "# Load Burmese mC4 subset\nfrom datasets import load_dataset\n\ndataset = load_dataset(\"allenai/c4\", \"my\", split=\"train\", streaming=True)\nfor example in dataset.take(3):\n    print(example[\"text\"])",
            kotlinCode = "// Parsing a JSONL stream of mC4\nfun parseJsonl(reader: BufferedReader) {\n    reader.forEachLine { line ->\n        val obj = JSONObject(line)\n        val text = obj.getString(\"text\")\n    }\n}"
        ),
        MyanmarDataset(
            name = "statmt-cc100 Burmese Dataset",
            category = "Web Crawl / Cleaned",
            contentType = "Web crawl plain text (Uni/ZG)",
            format = "TXT",
            size = "46M (Uni) / 178M (ZG)",
            sourceUrl = "https://data.statmt.org/cc-100/my.txt.xz",
            description = "Unicode နှင့် Zawgyi အုပ်စုနှစ်ခုလုံးအတွက် ရရှိနိုင်သော ကြီးမားသည့် ဝဘ် crawl ဒေတာစု ဖြစ်သည်။ encoding conversions, cleaning lab, and vocabulary induction လုပ်ငန်းများအတွက် အထူးသင့်လျော်သည်။",
            sampleFormat = "TXT",
            sampleData = "မြန်မာနိုင်ငံတွင် လေထုညစ်ညမ်းမှုသည် ကျန်းမာရေးကိစ္စဆိုင်ရာအန္တရာယ်တစ်ခုဖြစ်သည်။\nအထူးသဖြင့် ရန်ကုန်နှင့် မန္တလေးကဲ့သို့သော မြို့ကြီးများတွင် ပိုမိုဆိုးရွားပါသည်။",
            pythonCode = "# Download and extract statmt-cc100 xz file\nimport lzma\n\nwith lzma.open(\"my.txt.xz\", \"rt\", encoding=\"utf-8\") as f:\n    for i in range(5):\n        print(f.readline().strip())",
            kotlinCode = "// Read decompressed stream of text chunks\nfun readXzStream(inputStream: InputStream) {\n    // Use third-party XZ decoder or standard decompressed stream\n}"
        ),
        MyanmarDataset(
            name = "CulturaX Burmese Dataset",
            category = "Web Crawl / Cleaned",
            contentType = "Cleaned Web & News with URL",
            format = "JSON / Parquet",
            size = "866K rows",
            sourceUrl = "https://huggingface.co/datasets/uonlp/CulturaX",
            description = "သတင်းများနှင့် ဝဘ်စာမျက်နှာများမှ စုစည်းကာ အရည်အသွေးမြင့်မားစွာ သန့်စင်ထားသော text dataset ဖြစ်သည်။ URL နှင့် timestamp ပါဝင်သောကြောင့် Temporal evaluation နှင့် web search grounding အတွက် အသုံးပြုနိုင်သည်။",
            sampleFormat = "JSON",
            sampleData = "{\n  \"text\": \"ဒုံးကျည် ၂ စင်း မြောက်ကိုရီးယား ပစ်လွှတ်တဲ့အကြောင်းအပါအဝင် မနက်ခင်းသတင်းများ...\",\n  \"timestamp\": \"2019-08-23 04:58:02\",\n  \"url\": \"https://www.bbc.com/burmese/49108007\",\n  \"source\": \"BBC News မြန်မာ\"\n}",
            pythonCode = "# Load CulturaX Burmese from HuggingFace\nfrom datasets import load_dataset\n\ndataset = load_dataset(\"uonlp/CulturaX\", \"my\", split=\"train\")\nprint(dataset[0])",
            kotlinCode = "// Kotlin class mapped to CulturaX scheme\ndata class CulturaxItem(\n    val text: String,\n    val timestamp: String,\n    val url: String,\n    val source: String\n)"
        ),
        MyanmarDataset(
            name = "fineweb-2 Burmese Dataset",
            category = "Web Crawl / Cleaned",
            contentType = "Cleaned Web Crawl with metadata",
            format = "JSON / Parquet",
            size = "~1.64M rows",
            sourceUrl = "https://huggingface.co/datasets/HuggingFaceFW/fineweb-2",
            description = "FineWeb-2 crawl pipeline မှ ထွက်ပေါ်လာသည့် အဆင့်မြင့် သန့်စင်ပြီးသား (cleaned, deduplicated) မြန်မာစာစု ဖြစ်သည်။ LLM foundation training နှင့် high-quality text generation အတွက် အသုံးဝင်သည်။",
            sampleFormat = "JSON",
            sampleData = "{\n  \"text\": \"ပြသဆဲနှင့် ရုံတင်မည့် နိုင်ငံခြားရုပ်ရှင်မိတ်ဆက် ထောက်လှမ်းရေးအေဂျင်စီ...\",\n  \"id\": \"uuid-bf7oj3vzoqviwopglk\",\n  \"url\": \"https://example.com/movie-news\",\n  \"date\": \"2023-02-12 10:30:11\"\n}",
            pythonCode = "# Load FineWeb-2 Burmese portion\nfrom datasets import load_dataset\n\ndataset = load_dataset(\"HuggingFaceFW/fineweb-2\", \"my\", split=\"train\")\nprint(f\"Successfully loaded {len(dataset)} items\")",
            kotlinCode = "// Kotlin data class for FineWeb-2\ndata class FineWebItem(\n    val id: String,\n    val text: String,\n    val url: String,\n    val date: String\n)"
        ),
        MyanmarDataset(
            name = "ALT Parallel Corpus (ALT)",
            category = "Parallel / Translation",
            contentType = "Parallel sentence pairs",
            format = "CSV",
            size = "~20,000 sentences",
            sourceUrl = "https://live.european-language-grid.eu/catalogue/corpus/7733",
            description = "အာရှဘာသာစကားများ၏ parallel alignment dataset ဖြစ်ပြီး မြန်မာ-အင်္ဂလိပ် ဆက်စပ်မှုများ ပါဝင်သည်။ Machine Translation (MT) နှင့် cross-lingual Transfer learning တို့အတွက် အလွန်ကောင်းမွန်သည်။",
            sampleFormat = "CSV",
            sampleData = "\"my\",\"en\"\n\"သူသည် စာအုပ် တစ်အုပ် ဝယ်ခဲ့သည်။\",\"He bought a book.\"\n\"မြန်မာနိုင်ငံသည် လှပသော တိုင်းပြည်ဖြစ်သည်။\",\"Myanmar is a beautiful country.\"",
            pythonCode = "# Read ALT CSV in Python\nimport pandas as pd\n\ndf = pd.read_csv(\"alt_burmese_english.csv\")\nfor idx, row in df.iterrows():\n    print(f\"MY: {row['my']} | EN: {row['en']}\")",
            kotlinCode = "// Kotlin parsing helper for ALT CSV file\nfun parseAltCsv(line: String): Pair<String, String> {\n    val parts = line.split(\",\")\n    return Pair(parts[0].replace(\"\\\"\", \"\"), parts[1].replace(\"\\\"\", \"\"))\n}"
        ),
        MyanmarDataset(
            name = "UCSY Myanmar-English Parallel Corpus",
            category = "Parallel / Translation",
            contentType = "Parallel sentences for translation",
            format = "CSV / TXT",
            size = "200,000 sentences",
            sourceUrl = "http://onlineresource.ucsy.edu.mm/",
            description = "ရန်ကုန်ကွန်ပျူတာတက္ကသိုလ် (UCSY) NLP Lab မှ ထုတ်ဝေထားသော အကြီးမားဆုံး မြန်မာ-အင်္ဂလိပ် parallel sentences စုစည်းမှု ဖြစ်သည်။ neural machine translation (NMT) နှင့် lexical resources ဖွံ့ဖြိုးမှုအတွက် မရှိမဖြစ်လိုအပ်သည်။",
            sampleFormat = "CSV",
            sampleData = "\"my\",\"en\"\n\"ပြည်သူ့တပ်ဖွဲ့သည် မြန်မာနိုင်ငံ၏ တပ်ဖွဲ့ဖြစ်သည်။\",\"The People's Army is the military of Myanmar.\"\n\"ကျွန်ုပ်တို့သည် ပညာရေးကို တိုးမြှင့်ရမည်।\",\"We must promote education.\"",
            pythonCode = "# Load UCSY parallel dataset\nimport csv\n\nwith open(\"ucsy_parallel.csv\", mode='r', encoding='utf-8') as f:\n    reader = csv.reader(f)\n    for row in list(reader)[:5]:\n        print(f\"Myanmar: {row[0]} -> English: {row[1]}\")",
            kotlinCode = "// Load parallel corpus into local Room DB for offline translation model\n@Entity(tableName = \"ucsy_parallel\")\ndata class UcsyParallel(\n    @PrimaryKey(autoGenerate = true) val id: Int = 0,\n    val myText: String,\n    val enText: String\n)"
        ),
        MyanmarDataset(
            name = "Myanmar Dhamma Article Dataset",
            category = "Parallel / Translation",
            contentType = "Religious Audio & Text Metadata",
            format = "CSV / TXT",
            size = "30,000+ records",
            sourceUrl = "https://winmetta.org/dhamma-download/",
            description = "ဓမ္မ/တရားတော်စာသားများ၊ ဗုဒ္ဓဘာသာဆိုင်ရာ meta-data နှင့် audio link များ ပါဝင်သည်။ Spiritual domain adaptation, religious text analysis နှင့် domain-specific TTS/ASR validation တို့အတွက် ကောင်းမွန်သည်။",
            sampleFormat = "CSV",
            sampleData = "\"Title\",\"Speaker\",\"Language\",\"Type\",\"URL\"\n\"မေတ္တာသုတ်\",\"ပါမောက္ခချုပ်ဆရာတော်ကြီး\",\"မြန်မာ\",\"MP3\",\"http://www.dhammadownload.com/MettaSutta.mp3\"\n\"ဓမ္မစကြာ\",\"မင်းကွန်းဆရာတော်ကြီး\",\"မြန်မာ\",\"eBook\",\"http://www.dhammadownload.com/DhammaCakka.pdf\"",
            pythonCode = "# Parse Dhamma meta database\nimport pandas as pd\n\ndf = pd.read_csv(\"dhamma_articles.csv\")\nprint(df[df[\"Speaker\"] == \"ပါမောက္ခချုပ်ဆရာတော်ကြီး\"])",
            kotlinCode = "// Retrofit download utility for offline eBooks\nfun downloadDhammaBook(url: String) {\n    // Implementation to save PDF to folder\n}"
        ),
        MyanmarDataset(
            name = "Myanmar Dhamma Q&A Dataset",
            category = "Supervised & Instruction",
            contentType = "Religious Q&A Pairs",
            format = "JSON",
            size = "~1,000 Q&A pairs",
            sourceUrl = "https://winmetta.org/dhamma-download/",
            description = "ဗုဒ္ဓဘာသာဆိုင်ရာ အမေး-အဖြေများ၊ သံဃာတော်များနှင့် ဆရာတော်များ၏ တရားဓမ္မ ဖြေကြားချက်များကို စုစည်းထားသော dataset ဖြစ်သည်။ Domain-specific chatbot နှင့် instruction-tuning တို့တွင် အသုံးပြုနိုင်သည်။",
            sampleFormat = "JSON",
            sampleData = "[\n  {\n    \"question\": \"ဗုဒ္ဓဘုရား၏ သင်ကြားချက်များ၏ အဓိပ္ပါယ်မှာဘာလဲ?\",\n    \"answer\": \"အဓိပ္ပါယ်မှာ လူ့ဘဝအနတ်နဲ့ မလွှတ်နိုင်သော ဒုက္ခများကို ဖယ်ရှားဖို့ နည်းလမ်းဖော်ပြခြင်းဖြစ်ပါတယ်။\"\n  }\n]",
            pythonCode = "# Parse Dhamma Q&A JSON\nimport json\n\nwith open(\"dhamma_qa.json\", \"r\", encoding=\"utf-8\") as f:\n    data = json.load(f)\n    print(f\"Loaded {len(data)} Dhamma Q&As\")",
            kotlinCode = "// Model class to host local Dhamma QA chatbot knowledge base\ndata class DhammaQA(\n    val question: String,\n    val answer: String\n)"
        ),
        MyanmarDataset(
            name = "Myanmar Social Media Sentiment Dataset",
            category = "Supervised & Instruction",
            contentType = "Sentiment Labeled Sentences",
            format = "CSV",
            size = "732 annotated rows",
            sourceUrl = "https://huggingface.co/datasets/chuuhtetnaing/myanmar-social-media-sentiment-analysis-dataset",
            description = "Social media post များအပေါ်တွင် sentiment (Positive/Negative/Neutral) tag လုပ်ထားသော dataset ဖြစ်သည်။ Text classification, sentiment analysis, opinion mining, and content filter စနစ်များ သင်ကြားရာတွင် သုံးသည်။",
            sampleFormat = "CSV",
            sampleData = "\"Text\",\"Text-MM\",\"Sentiment\"\n\"Enjoying a beautiful day at the park!\",\"ပန်းခြံထဲမှာ လှပတဲ့နေ့လေးတစ်နေ့ကို ဖြတ်သန်းနေပါတယ်!\",\"Positive\"\n\"The service is extremely slow and bad.\",\"ဝန်ဆောင်မှုက တော်တော်လေး နှေးကွေးပြီး ဆိုးရွားပါတယ်။\",\"Negative\"",
            pythonCode = "# Sentiment analysis pre-processing with Pandas\nimport pandas as pd\n\ndf = pd.read_csv(\"hf://datasets/chuuhtetnaing/myanmar-social-media-sentiment-analysis-dataset/data.csv\")\nprint(df['Sentiment'].value_counts())",
            kotlinCode = "// Classifier state mapper\nenum class Sentiment { POSITIVE, NEGATIVE, NEUTRAL }\ndata class AnnotatedPost(val text: String, val sentiment: Sentiment)"
        ),
        MyanmarDataset(
            name = "myXNLI Natural Language Inference Corpus",
            category = "Supervised & Instruction",
            contentType = "NLI Annotated Pairs",
            format = "JSONL",
            size = "7,500 (dev/test), 392,702 (train)",
            sourceUrl = "https://huggingface.co/datasets/akhtet/myXNLI",
            description = "Natural Language Inference (NLI) label များ (Entailment/Contradiction/Neutral) ကို မြန်မာဘာသာဖြင့် လူကိုယ်တိုင် ပြန်ဆိုထားသော dataset ဖြစ်သည်။ Logical reasoning, cross-lingual transfer, and evaluation benchmarks တို့တွင် အလွန်အသုံးဝင်သည်။",
            sampleFormat = "JSON",
            sampleData = "{\n  \"genre\": \"government\",\n  \"label\": \"neutral\",\n  \"sentence1_my\": \"သဘောတရားအရ ခရင်မ်စိမ်ခြင်းတွင် အခြေခံအတိုင်းအတာ နှစ်ခုရှိသည် - ထုတ်ကုန်နှင့် ပထဝီဝင်။\",\n  \"sentence2_my\": \"ထုတ်ကုန်နှင့် ပထဝီဝင်အနေအထားသည် ခရင်မ် skimming ကို အလုပ်ဖြစ်စေသည်။\"\n}",
            pythonCode = "# Load myXNLI dataset\nfrom datasets import load_dataset\n\ndataset = load_dataset(\"akhtet/myXNLI\")\nprint(dataset[\"test\"][0])",
            kotlinCode = "// NLI inference logic model mapping\ndata class NliPair(\n    val label: String,\n    val sentence1: String,\n    val sentence2: String\n)"
        ),
        MyanmarDataset(
            name = "Myanmar Instruction Tuning Dataset",
            category = "Supervised & Instruction",
            contentType = "Instruction QA (inputs/targets)",
            format = "Parquet / JSON",
            size = "17.4k rows",
            sourceUrl = "https://huggingface.co/datasets/chuuhtetnaing/myanmar-instruction-tuning-dataset",
            description = "မြန်မာဘာသာစကားအခြေပြု instruction-following dataset ဖြစ်ပြီး စိုက်ပျိုးရေး၊ အထွေထွေဗဟုသုတ နှင့် မေးခွန်း-အဖြေများ ပါဝင်သည်။ LLMs instruction-tuning လုပ်ရန်အတွက် အဓိကအသုံးပြုသည်။",
            sampleFormat = "JSON",
            sampleData = "{\n  \"inputs\": \"သစ်ပင်စိုက်ပျိုးရန် အကောင်းဆုံးအချိန်က ဘယ်အချိန်လဲ?\",\n  \"targets\": \"မိုးရာသီအစပိုင်းသည် သစ်ပင်စိုက်ပျိုးရန် အကောင်းဆုံးအချိန်ဖြစ်သည်\",\n  \"source\": \"jojo-ai-mst/Myanmar-Agricutlure-1K\"\n}",
            pythonCode = "# Load Instruction Tuning Dataset in PyTorch\nfrom datasets import load_dataset\n\ndataset = load_dataset(\"chuuhtetnaing/myanmar-instruction-tuning-dataset\", split=\"train\")\nprint(f\"Sample prompt: {dataset[0]['inputs']} -> Response: {dataset[0]['targets']}\")",
            kotlinCode = "// Room-ready Instruction model\n@Entity(tableName = \"instruction_tuning\")\ndata class InstructionPair(\n    @PrimaryKey(autoGenerate = true) val id: Int = 0,\n    val inputs: String,\n    val targets: String,\n    val source: String\n)"
        ),
        MyanmarDataset(
            name = "mySentence Corpus (Tokenizer)",
            category = "Supervised & Instruction",
            contentType = "Sentence & CRF Tokenization",
            format = "TXT / CSV",
            size = "40k+ sentences (580k+ tagged units)",
            sourceUrl = "https://github.com/ye-kyaw-thu/mySentence",
            description = "မြန်မာစာကြောင်းခွဲခြားခြင်း (sentence segmentation/tokenization) အတွက် tagging tag များ (B/O/N/E, CRF style) ပါဝင်သော dataset ဖြစ်သည်။ text preprocessing, tokenizer, parsing validation တို့အတွက် မရှိမဖြစ်အသုံးဝင်သည်။",
            sampleFormat = "TXT",
            sampleData = "ကျွန်တော်/B ပျင်း/N လာ/N ပြီ/E",
            pythonCode = "# Token split using slash annotation\nsample_str = \"ကျွန်တော်/B ပျင်း/N လာ/N ပြီ/E\"\ntokens = [t.split(\"/\")[0] for t in sample_str.split()]\nprint(\"Tokens:\", tokens)",
            kotlinCode = "// Kotlin tokenizer split simulation\nfun tokenizeMyanmar(text: String): List<String> {\n    return text.split(\" \") // Simplified word segmentation simulation\n}"
        ),
        MyanmarDataset(
            name = "Archive.org Myanmar Text Datasets",
            category = "Monolingual",
            contentType = "Scanned Historical Text & Books",
            format = "TXT / PDF",
            size = "Various books & newspapers",
            sourceUrl = "https://archive.org/details/texts",
            description = "မိဘအဘိဓာန်၊ ဘာသာပြန်ကျမ်းများ (ကျမ်းစာ၊ Quran၊ Bible)၊ သတင်းစာများ၊ သိပ္ပံစာအုပ်များနှင့် သမိုင်းဝင် ဆောင်းပါးများ ပါဝင်သည်။ Domain adaptation, vocabulary extension, and style transfer တို့အတွက် အသုံးဝင်သည်။",
            sampleFormat = "TXT",
            sampleData = "ခေတ်ဟောင်းသတင်းစာများနှင့် မြန်မာစာပေဆိုင်ရာ သိပ္ပံနှင့်သင်္ချာ ဆောင်းပါးများ။",
            pythonCode = "# Load raw books from Archive.org downloads\nimport urllib.request\n\nurl = \"https://archive.org/stream/BurmeseDictionary/burmese_dict_djvu.txt\"\n# download or read string content\n",
            kotlinCode = "// Download manager utility to fetch books dynamically\nfun fetchBookStream(urlStr: String): InputStream {\n    val url = URL(urlStr)\n    return url.openStream()\n}"
        ),
        MyanmarDataset(
            name = "Myanmar Language Dataset Collection (GitHub)",
            category = "Monolingual",
            contentType = "Meta collection list",
            format = "Readme / Repo links",
            size = "Universal NLP index",
            sourceUrl = "https://github.com/ye-kyaw-thu",
            description = "မြန်မာ NLP dataset များနှင့် speech, text, parallel, QA, C4, Sentiment စသည်တို့ကို စုစည်းထားသော meta github repository ဖြစ်သည်။ သုတေသနပညာရှင်များအတွက် အမြန်ရှာဖွေရေး ဗဟိုချက်ဖြစ်သည်။",
            sampleFormat = "TXT",
            sampleData = "GitHub link collections for all modern Myanmar NLP libraries, dictionaries and speech resources.",
            pythonCode = "# Clone various repository assets via subprocess\nimport subprocess\nsubprocess.run([\"git\", \"clone\", \"https://github.com/ye-kyaw-thu/mySentence.git\"])",
            kotlinCode = "// Open GitHub repository URL inside external browser intent\nfun openGitHubRepo(context: Context) {\n    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(\"https://github.com/ye-kyaw-thu\"))\n    context.startActivity(intent)\n}"
        ),
        MyanmarDataset(
            name = "Leipzig Corpora (mya-MM_web_2019)",
            category = "Web Crawl / Cleaned",
            contentType = "Web corpus with frequency",
            format = "TXT / CSV",
            size = "23k sentences (High Quality)",
            sourceUrl = "https://corpora.uni-leipzig.de/en?corpusId=mya-mm_web_2019",
            description = "ဂျာမနီနိုင်ငံ Leipzig တက္ကသိုလ်မှ စုစည်းထားသော အရည်အသွေးမြင့်မားသည့် ဝဘ် crawl မြန်မာစာစု ဖြစ်သည်။ စာလုံးကြိမ်နှုန်း၊ ပတ်ဝန်းကျင်ဆက်စပ်မှုများနှင့် dictionary validation တို့အတွက် ကောင်းမွန်သည်။",
            sampleFormat = "CSV",
            sampleData = "\"id\",\"text\"\n\"1\",\"မြန်မာနိုင်ငံတော်၏ မြို့တော်မှာ နေပြည်တော် ဖြစ်သည်။\"\n\"2\",\"နည်းပညာတိုးတက်မှုသည် လူသားတို့၏ စွမ်းဆောင်ရည်ကို မြှင့်တင်ပေးသည်။\"",
            pythonCode = "# Load Leipzig text file\nwith open(\"mya-mm_web_2019_sentences.txt\", \"r\", encoding=\"utf-8\") as f:\n    for line in list(f)[:5]:\n        parts = line.strip().split(\"\\t\")\n        print(f\"ID: {parts[0]} -> Text: {parts[1]}\")",
            kotlinCode = "// Kotlin Leipzig parser mapping\ndata class LeipzigSentence(val id: Int, val text: String)"
        )
    )

    // A simple offline Zawgyi to Unicode Converter approximation
    fun convertZawgyiToUnicode(input: String): String {
        if (input.isEmpty()) return ""
        // Replace typical Zawgyi patterns with Unicode
        var out = input
        // Simulate Zawgyi-Unicode conversions
        out = out.replace("္", "") // Remove duplicate subjoined chars if any
        out = out.replace("ေရ", "ရေ")
        out = out.replace("သို႔", "သို့")
        out = out.replace("ႏိုင္", "နိုင်")
        out = out.replace("ျမန္", "မြန်")
        out = out.replace("ျပည္", "ပြည်")
        out = out.replace("မွ", "မှ")
        out = out.replace("ၿပီး", "ပြီး")
        out = out.replace("ေန", "နေ")
        out = out.replace("ေတာ္", "တော်")
        out = out.replace("ရန္", "ရန်")
        out = out.replace("ဂ်", "ဂျ")
        out = out.replace("ၿခား", "ခြား")
        out = out.replace("တို႔", "တို့")
        out = out.replace("ေျပာ", "ပြော")
        out = out.replace("နိူင်", "နိုင်")
        return out
    }

    // A simple sentence splitting simulator based on 'mySentence' behavior
    fun segmentSentences(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val punctuations = listOf("။", "၊", "?", "!", "\n")
        var current = ""
        val list = mutableListOf<String>()
        for (char in text) {
            current += char
            if (punctuations.contains(char.toString())) {
                if (current.trim().isNotEmpty()) {
                    list.add(current.trim())
                }
                current = ""
            }
        }
        if (current.trim().isNotEmpty()) {
            list.add(current.trim())
        }
        return list.ifEmpty { listOf(text) }
    }
}
