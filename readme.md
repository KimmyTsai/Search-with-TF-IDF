# Search with TF-IDF

## 簡介

本專案實作了一個簡單的搜尋引擎，使用 TF-IDF（詞頻-逆文件頻率）來計算文本中的關鍵字權重，並進行搜尋和排序。此程式會處理一個文本檔案，建立索引並儲存索引資料，然後可以基於查詢進行文檔搜尋。

## 檔案結構

- `BuildIndex.java`: 建立文本的索引，並將索引序列化保存。
- `TFIDFSearch.java`: 基於使用者輸入的查詢進行 TF-IDF 搜尋。

## 安裝與執行步驟

1. 編譯 Java 程式：

   ```bash
   javac BuildIndex.java TFIDFSearch.java
   ```

2. 建立索引：

   - 準備文本檔案 `corpus0.txt`，範例內容如下：

     ```css
     1 Between subtle shading and the absence of light lies the nuance of iqlusion.
     2 It was totally invisible How's that possible? They used the Earths magnetic field X
     3 The information was gathered and transmitted underground to an unknown location X
     4 Does Langley know about this? They should Its buried out there somewhere X
     5 Who knows the exact location? Only WW This was his last message X
     6 Thirty eight degrees fifty seven minutes six point five seconds north
     7 Seventy seven degrees eight minutes forty four seconds west ID by rows
     8 Slowly, desparately slowly, the remains of passage debris
     9 that encumbered the lower part of the doorway was removed
     10 with trembling hands.
     11 I made a tiny breach in the upper left-hand corner
     12 and then widening the hole a little, I inserted the candle and peered in.
     13 The hot air escaping from the chamber caused the flame to flicker
     14 but presently details of the room within emerged from the mist X
     15 Can you see anything Q?
     ```

   - 執行建索引指令：

     ```bash
     java BuildIndex corpus0.txt
     ```

   - 處理後的文本結果如下，儲存至`processed_docs.txt`：

    ```css
    between subtle shading and the absence of light lies the nuance of iqlusion 
    it was totally invisible how s that possible they used the earths magnetic field x 
    the information was gathered and transmitted underground to an unknown location x 
    does langley know about this they should its buried out there somewhere x 
    who knows the exact location only ww this was his last message x 
    thirty eight degrees fifty seven minutes six point five seconds north 
    seventy seven degrees eight minutes forty four seconds west id by rows 
    slowly desparately slowly the remains of passage debris 
    that encumbered the lower part of the doorway was removed 
    with trembling hands 
    i made a tiny breach in the upper left hand corner 
    and then widening the hole a little i inserted the candle and peered in 
    the hot air escaping from the chamber caused the flame to flicker 
    but presently details of the room within emerged from the mist x 
    can you see anything q 
    ```

   - 進行序列化，儲存至`corpus0`

3. 進行查詢

    - 執行查詢指令，將查詢條件 exampleTC.txt 輸入：

    ```bash
    java TFIDFSearch corpus0 exampleTC.txt
    ```

    `exampleTC.txt` 範例查詢內容：

    ```bash
    3
    the AND x
    and OR was
    ```

4. 查詢結果範例：

    `Query:`the AND x`

    ```yaml
    Document 0 TF-IDF: 0.024573642915646326
    Document 2 TF-IDF: 0.007508613113114155
    排序結果: [0, 2, -1]
    ```

    `Query:`and OR was`

    ```yaml
    Document 0 TF-IDF: 0.030717053644557908
    Document 1 TF-IDF: 0.009215116093367373
    Document 2 TF-IDF: 0.01501722622622831
    排序結果: [0, 2, 1]
    ```

5. 查詢結果會輸出到 `output.txt`：

    ```
    0 2 -1
    0 2 1
    ```

## 結果

``output.txt` 包含基於查詢的結果，使用 TF-IDF 演算法計算出最相關的文檔，並將結果排序後輸出。

## 備註

`該程式支援 AND/OR 查詢，並根據 TF-IDF 值對文檔進行排序。
`如有多個查詢，會根據輸入順序逐個執行並輸出結果。
