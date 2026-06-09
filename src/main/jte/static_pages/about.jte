@param String imprintName = ""
@param String imprintStreet = ""
@param String imprintZipcode = ""
@param String imprintCity = ""
@param String imprintPhone = ""
@param String imprintMail = ""
@param String danger = null
@template.layout(title = "Über | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-bold text-left mt-3 mb-3">Über <i>Ranking-Info</i></h1>
    <p class="mb-3">
        Ranking-Info ist eine Webanwendung, die Daten der Aktiven und Jugendrangliste des Deutschen Tennis Bundes (DTB)
        auf verschiedene Arten visualisiert.
        Alle Daten sind der offiziellen Rangliste entnommen, die in der jeweils aktuellen Fassung unter<br>
        <a href="https://www.tennis.de/spielen/ranglisten-und-leistungsklasse.html" target="_blank" class="text-brand">
            https://www.tennis.de/spielen/ranglisten-und-leistungsklasse.html
        </a><br>abrufbar ist.
    </p>
    <h1 class="text-2xl font-bold text-left mt-3 mb-3">Impressum</h1>
    <p>
        <strong>${imprintName}</strong><br>
        <span>${imprintStreet}</span><br>
        <span>${imprintZipcode} ${imprintCity}</span><br>
        <a href="tel:${imprintPhone}" class="text-brand">${imprintPhone}</a><br>
        <a href="mailto:${imprintMail}" class="text-brand">${imprintMail}</a>
    </p>
`)
