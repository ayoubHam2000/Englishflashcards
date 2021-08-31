package com.example.englishflashcards.Utilities

//sets(name, createdTime, tagColor)
//collections(name, createdTime, father, isHide, lastViewTime, tableName lastModifiedTime)
//words(name isFavorite isHide type origin lastView level repetition isShowed isRemember isDeleted lastModifiedTime)
//examples(word example)
//definitions(word definition)
//tableName (name , frequency , createdTime)
//texts (collection , text )
//vars (name, value)
//cWords(name , frequency , createdTime)

//table Name
const val Sets = "Sets"
const val Collections = "Collections"
const val Words = "Words"
const val Examples = "Examples"
const val Definition = "Definition"
const val TableName = "TableNames"
const val Texts = "Texts"
const val Vars = "Vars"
const val CWords = "CWords"
const val DictionaryDb = "Dictionary"

//Var names
const val D_name = "name"
const val D_createdTime = "createdTime"
const val D_tagColor = "tagColor"
const val D_example = "example"
const val D_definition = "definition"
const val D_frequency = "frequency"
const val D_collection = "collection"
const val D_text = "text"
const val D_isHide = "isHide"
const val D_value = "value"
const val D_lastViewTime = "lastViewTime"
const val D_level = "level"
const val D_tableName = "tableName"
const val D_isFavorite = "isFavorite"
const val D_repetition = "repetition"
const val D_type = "type"
const val D_origin = "origin"
const val D_isShowed = "isShowed"
const val D_isRemember = "isRemember"
const val D_isDeleted = "isDeleted"
const val D_lastModifiedTime = "lastModifiedTime"
const val D_father = "father"

//table declaration
const val DT_Set = "$Sets ($D_name VARCHAR, $D_createdTime BIGINT, $D_tagColor INT)"
const val DT_Collections = "$Collections ($D_name VARCHAR, $D_createdTime BIGINT, $D_father VARCHAR, $D_isHide BIT DEFAULT 0, $D_lastViewTime BIGINT DEFAULT 0, $D_tableName VARCHAR, $D_lastModifiedTime BIGINT DEFAULT 0)"
const val DT_Words = "$Words ($D_name VARCHAR, $D_isFavorite BIT DEFAULT 0, $D_isHide BIT DEFAULT 0, $D_type VARCHAR DEFAULT '', $D_origin VARCHAR DEFAULT '', $D_lastViewTime BIGINT DEFAULT 0, $D_level INT DEFAULT 0, $D_repetition INT DEFAULT 0, $D_isShowed BIT DEFAULT 0, $D_isRemember BIT DEFAULT 0, $D_isDeleted BIT DEFAULT 0, $D_lastModifiedTime BIGINT DEFAULT 0)"
const val DT_Examples = "$Examples ($D_name VARCHAR, $D_example VARCHAR)"
const val DT_Definition = "$Definition ($D_name VARCHAR, $D_definition VARCHAR)"
const val DT_Texts = "$Texts ($D_name VARCHAR, $D_text VARCHAR)"
const val DT_Vars = "$Vars ($D_name VARCHAR, $D_value VARCHAR, UNIQUE($D_name))"
const val DT_CWords = "$CWords ($D_name VARCHAR, $D_frequency INT, $D_createdTime BIGINT)"
const val DT_TableName = "($D_name VARCHAR, $D_frequency INT, $D_createdTime BIGINT)"
const val DT_Dictionary = "$DictionaryDb ($D_name VARCHAR, $D_definition VARCHAR)"


val DV_Set = arrayOf(D_name, D_createdTime, D_tagColor)
val DV_Collections = arrayOf(D_name ,D_createdTime ,D_father ,D_isHide ,D_lastViewTime ,D_tableName ,D_lastModifiedTime)
val DV_Words = arrayOf(D_name ,D_isFavorite ,D_isHide ,D_type ,D_origin ,D_lastViewTime ,D_level ,D_repetition ,D_isShowed ,D_isRemember ,D_isDeleted ,D_lastModifiedTime)
val DV_Examples = arrayOf(D_name ,D_example)
val DV_Definition = arrayOf(D_name ,D_definition)
val DV_Texts = arrayOf(D_name ,D_text)
val DV_Vars = arrayOf(D_name ,D_value)
val DV_CWords = arrayOf(D_name ,D_frequency ,D_createdTime)
val DV_TableName = arrayOf(D_name ,D_frequency ,D_createdTime)

const val DC_Words = "($D_name VARCHAR, $D_isFavorite BIT DEFAULT 0, $D_isHide BIT DEFAULT 0, $D_type VARCHAR DEFAULT '', $D_origin VARCHAR DEFAULT '', $D_lastViewTime BIGINT DEFAULT 0, $D_level INT DEFAULT 0, $D_repetition INT DEFAULT 0, $D_isShowed BIT DEFAULT 0, $D_isRemember BIT DEFAULT 0, $D_isDeleted BIT DEFAULT 0, $D_lastModifiedTime BIGINT DEFAULT 0)"
