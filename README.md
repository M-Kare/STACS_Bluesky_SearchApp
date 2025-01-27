# Bluesky Account
### Passwort
Das Passwort für den BlueSky-Account muss als Environment Variable (unter BSPASS) hinterlegt werden. In IntelliJ macht man das, indem man neben dem "Play-Button" auf die 3 Punkte geht, edit auswählt.
Es öffnet sich die Run/Debug Configuration. Dort trägt man die Variable unter Environment Variables ein: BSPASS=fabianJunkert
Falls das Feld für Environment Variables nicht da ist, kann man es einblenden, indem man es unter "Modify options" auswählt (rechts neben Build and run im Run/Debug Configuration Fenster).
### Benutzer
Als Standard wird unser Benutzer verwendet (stackingstacs.bsky.social). Dieser kann geändert werden, wenn man die Environment Variable BSNAME anlegt.
Dort muss der Handle des Accounts hinterlegt werden.  
  
Der Account ist nötig für folgende Features: "Posts posten", "My Posts" (Standard ist StackingStacs Account)  
Außerdem nötig für die Backup-such-funktion, falls der normale BlueSky searchPosts Endpunkt wieder Probleme macht.

# Gemini API
Um die Gemini API für Advanced Search und Zusammenfassung nutzen zu können, muss ein API-Key unter der Environtment-Variable GOOGLE_API_KEY hinterlegt werden.
Hinterlegen der Variable in IntelliJ wie oben bereits genannt.  
Der Key ist nötig für folgende Features: "Summarize Posts", "Find similar posts"

# Starten
- mit Backend- & Frontend-Server
  - Docker starten 
  - Vue-Frontend starten (npm i & npm run dev)
  - Anwendung starten
  - verbinden über localhost:5173
- nur mit Backend-Server
  - Docker starten
  - aktuelles Frontend ins Backend kopieren (falls noch nicht drin)
    - cd frontend
    - npm i
    - npm run build
    - Inhalt des dist-Ordner nach ins Backend nach Ressources/public kopieren
  - starten des Backends
    - per ./gradlew bootRun oder IntelliJ
  - verbinden über localhost:8080

# Bedienung
- Posts ansehen
  - die rechte Liste der Posts zeigt standartmäßig die 300 aktuellsten Posts der Datenbank an
  - durch anklicken gelangt man direkt zum geklickten Post auf BlueSky
- Posts suchen
  - normal
    - Suchwort in Textbox eingeben und mit ok-Button bestätigen
  - erweitert
    - via expand checkbox und dann normal suchen oder Find similar posts button drücken
  - filtern
    - durch auswählen wird live gefiltert. Durch bestätigen mit ok-Button werden Posts passend zu den Filtern geholt
    - Datum -> zeigt Posts von dem Datum und früher (until)
    - Sprache (SelectBox) -> zeigt Posts, die die Sprache als Tag haben (laut BlueSky) 
  - X-Button
    - setzt Filter zurück und holt die aktuellsten 300 posts der Datenbank
- Eigene Posts anzeigen
  - mit hilfe der Radio-Buttons über den Posts umschalten zwischen normalen Posts und eigenen Posts
- Post posten
  - mit dem Textfeld "Create a Post" kann ein eigener Post auf den hinterlegten BlueSky-Account geschrieben werden
  - über den "Submit Post"-Button wird der Text auf BlueSky gepostet
- Summarize Posts
  - per Button "Summarize Posts" werden alle Posts, die gerade rechts angezeigt werden, zusammengefasst
- Sentiment
  - der PieChart zeigt die Sentiment-Verteilung der Posts aus der Liste an
  - jeder Post wurde von der StanfordCoreNLP bewertet
  - 0 = sehr negativ
  - 1 = negativ 
  - 2 = neutral
  - 3 = positiv
  - 4 = sehr positiv