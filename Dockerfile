FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Cài đặt plugin ICU (Hỗ trợ tiếng Việt và đa ngôn ngữ rất tốt)
RUN bin/elasticsearch-plugin install --batch analysis-icu