namespace java com.km.taste.thrift

service Store {
  binary get(1: binary request)
  binary put(1: binary request)
  binary remove(1: binary request)
  void open()
  void close()
}


struct Field {
  1: string name
  2: string value
}

struct Result {
  1: string key
  2: double score
}

struct Document {
  1: string key 
  2: list<Field> fields
}

struct Term {
  1: string name
  2: string value
}

struct Query {
  1: string content
}

struct LucenePacket {
  1: optional list<Document> documents
  2: optional Query query
  3: optional list<Term> terms
  4: optional i32 counter
  5: optional list<Result> results
}