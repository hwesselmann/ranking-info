@param String danger = null
@template.layout(title = "Hilfe | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-bold text-left mb-3 mt-3 tracking-normal">Hilfe</h1>
    <p class="mb-3">
        Hier findest Du Antworten auf einige oft gestellte Fragen. Die Anwendung möchte so selbsterklärend wie möglich
        sein, aber wie es beim Tennis eben mal ist: Bei einigen Dingen muss man Bescheid wissen, wie etwas funktioniert
        oder gedacht ist. Bei weiteren Fragen schreibe gerne eine E-Mail an ranking-info at h-dawg.de
    </p>

    <h2 class="text-lg font-semibold text-left mb-3 mt-4 text-gray-800">Spielersuche</h2>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Was ist die DTB-ID und wo finde ich sie?</h3>
    <p class="mb-3">
        Die DTB-ID ist eine 8-stellige Zahl, die jeden Tennisspieler im DTB lebenslang eindeutig identifiziert (so etwas wie die Steuer-ID für Tennisspieler).
        Sie kann von den Verantwortlichen eines Tennisvereins vergeben werden und ist vereinsunabhängig. Du kannst sie bei Deinem Verein erfragen oder auch auf
        Deinem Profil bei mybigpoint sehen.
    </p>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Kann ich die Suchfelder kombiniert nutzen?</h3>
    <p class="mb-3">
        Zum Teil. Name und Geburtsjahrgang lassen sich gemeinsam nutzen. Das ist praktisch, wenn Du nur einen Namensteil in die Suche eingibst oder
        Du einen sehr verbreiteten Nachnamen hast. Die DTB-ID kann nur einzeln als Suchkriterium genutzt werden. Hier bin ich davon ausgegangen, dass
        der Benutzer sie kennt (weil er zum Beispiel gerade vor dem Tableau eines Turniers steht und abschreiben kann).
    </p>

    <h2 class="text-lg font-semibold text-left mb-3 mt-4 text-gray-800">Spielerdetails</h2>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Warum stimmt meine Ranglistenposition unter „Aktuelle Position" nicht mit meiner Ranglistenposition überein?</h3>
    <p class="mb-3">
        Das ist ein bisschen gemein: Die Position in der Jahrgangsrangliste betrachtet immer nur einen Doppeljahrgang. Leider kann ein jüngerer Spieler durchaus mehr
        Punkte erspielt haben als Du. Weil die Positionen über alle Kinder und Jugendlichen auf der deutschen Rangliste errechnet werden,
        kann es also sein, dass die Gesamtposition in der entsprechenden Altersklasse „schlechter" ist als auf der Jahrgangsrangliste.
    </p>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Was bedeuten die Aufstiegs-/Abstiegssymbole neben den Platzierungen und Punkten?</h3>
    <p class="mb-3">
        Die Symbole zeigen Dir an, wie sich Deine Position in der Altersklasse – bezogen auf alle Einträge der Rangliste – im Vergleich zur
        letzten Ranglistenveröffentlichung verändert hat. Bei den Punkten ist das natürlich absolut. In den einzelnen Altersklassen kann es natürlich sein,
        dass Du in einer Altersklasse Positionen gutgemacht hast, während Du in einer anderen verloren hast (weil eben alle Spielerinnen bzw. Spieler betrachtet
        werden und nicht nur diejenigen, deren Geburtsjahrgang zu der jeweiligen Altersklasse gehört).
    </p>

    <h2 class="text-lg font-semibold text-left mb-3 mt-4 text-gray-800">Ranglisten</h2>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Wie bekomme ich die Jahresabschluss-Rangliste angezeigt?</h3>
    <p class="mb-3">
        Wenn Du ein Abschlussquartal auswählst und dann die Checkbox „Abschlussrangliste" anwählst, erhältst Du die Rangliste vor dem Altersklassenwechsel!
    </p>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Was bewirkt die Einstellung „nur Geburtsjahrgang"?</h3>
    <p class="mb-3">
        Bei der Auswahl werden nun nur die Spieler und Spielerinnen angezeigt, die nach dem Geburtsjahrgang her in der Altersklasse sind.
        Im Kalenderjahr 2020 erhält man bei Auswahl „U11 Juniorinnen" und „nur Geburtsjahrgang" nur die Spielerinnen des Geburtsjahrgangs 2009.
        Alle anderen Auswahlfilter werden natürlich trotzdem mit angewendet.
    </p>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Was bewirkt die Einstellung „auch Jüngere mit einbeziehen"?</h3>
    <p class="mb-3">
        Diese Auswahl zeigt nicht nur die Jahrgangsrangliste, sondern bezieht alle jüngeren Spielerinnen und Spieler mit ein. Das ist nützlich,
        wenn Du in einer höheren Altersklasse spielen willst und neugierig bist, ob Du es auf die Zulassungsliste schaffen kannst.
        Alle anderen Auswahlfilter werden natürlich trotzdem mit angewendet.
    </p>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Werden die Neueinstufungen zur Rangliste nachträglich mit einberechnet?</h3>
    <p class="mb-3">
        Manchmal passieren Fehler bei der Berechnung der offiziellen Rangliste und einige Spielerinnen und Spieler sind nicht einbezogen oder
        einige Ergebnisse wurden nicht mit berechnet. Dann macht der DTB eine Neueinstufung für Turnierveranstalter, die separat veröffentlicht wird.
        Solange der DTB danach keine neue Rangliste veröffentlicht, kann <em>Ranking-Info</em> diese auch nicht mit einbeziehen.
    </p>

    <h2 class="text-lg font-semibold text-left mb-3 mt-4 text-gray-800">Vereinsübersicht</h2>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Welche Spieler tauchen bei den Vereinen auf?</h3>
    <p class="mb-3">
        Die Vereinsübersicht zeigt alle Spielerinnen und Spieler, die im aktuellen Quartal auf der Rangliste unter diesem Verein geführt werden.
        Historische Daten werden hier nicht mit angezeigt!
    </p>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Warum kann ich einen Verein in der Vereinssuche nicht immer finden?</h3>
    <p class="mb-3">
        Hat ein Verein im aktuellen Quartal keinen Spieler und keine Spielerin auf der Rangliste, taucht er in der Suche auch nicht auf.
        Das kann sich natürlich in jedem Quartal ändern, so dass ein Verein im alten Quartal auftauchte, im aktuellen aber vielleicht nicht mehr.
    </p>

    <h2 class="text-lg font-semibold text-left mb-3 mt-4 text-gray-800">Verbände</h2>

    <h3 class="text-base font-semibold text-left mb-2 mt-3">Was sehe ich in der Verbandsübersicht?</h3>
    <p class="mb-3">
        Die Verbandsübersicht zeigt für jeden Doppeljahrgang die Anzahl der Spielerinnen und Spieler, die für die jeweiligen Landesverbände auf der Rangliste geführt werden.
    </p>

    <h2 class="text-lg font-semibold text-left mb-3 mt-4 text-gray-800">Bedienung auf kleinen Bildschirmen</h2>
    <p class="mb-5">
        <em>Ranking-Info</em> funktioniert am besten auf einem normalgroßen Bildschirm. Auch auf den meisten Tablets lässt es sich gut bedienen. Die meisten Ansichten
        sind auch für kleine Bildschirme, z.&#8239;B. auf Smartphones, optimiert. Manchmal bedingt die Menge der anzuzeigenden Daten aber, dass Du auch mal nach rechts scrollen musst.
        An der Mobiloptimierung wird kontinuierlich gearbeitet.
    </p>
`)
