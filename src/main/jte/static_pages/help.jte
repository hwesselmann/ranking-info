@param String danger = null
@template.layout(title = "Hilfe | Ranking-Info", danger = danger, content = @`
    <h1 class="text-2xl font-semibold mt-5 mb-1">Hilfe</h1>
    <p class="text-sm text-muted leading-relaxed mb-6 max-w-2xl">
        Antworten auf häufige Fragen. Die Anwendung möchte so selbsterklärend wie möglich sein — bei
        weiteren Fragen schreibe gerne eine E-Mail an contact at ranking-info.de
    </p>

    <div class="grid grid-cols-1 md:grid-cols-[170px_minmax(0,1fr)] gap-6 md:gap-8 pb-8">
        <nav class="ri-toc self-start md:sticky md:top-4" aria-label="Themen">
            <p class="ri-toc-title">Themen</p>
            <a href="#hilfe-spielersuche">Spielersuche</a>
            <a href="#hilfe-spielerdetails">Spielerdetails</a>
            <a href="#hilfe-ranglisten">Ranglisten</a>
            <a href="#hilfe-vereine">Vereine</a>
            <a href="#hilfe-verbaende">Verbände</a>
            <a href="#hilfe-mobile">Mobile Nutzung</a>
        </nav>

        <div>
            <section id="hilfe-spielersuche" class="mb-7 scroll-mt-4">
                <p class="ri-section-label">Spielersuche</p>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Was ist die DTB-ID
                        und wo finde ich sie?</p>
                    <p class="ri-faq-a">Die DTB-ID ist eine 8-stellige Zahl, die jeden Tennisspieler
                        im DTB lebenslang eindeutig
                        identifiziert (so etwas wie die Steuer-ID für Tennisspieler). Sie ist
                        vereinsunabhängig — Du kannst
                        sie bei Deinem Verein erfragen oder auf Deinem Profil bei mybigpoint
                        sehen.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Kann ich die
                        Suchfelder kombiniert nutzen?</p>
                    <p class="ri-faq-a">Zum Teil. Name und Geburtsjahrgang lassen sich gemeinsam
                        nutzen — praktisch bei einem
                        Namensteil oder einem sehr verbreiteten Nachnamen. Die DTB-ID kann nur
                        einzeln als Suchkriterium
                        genutzt werden (etwa wenn Du sie vom Tableau eines Turniers
                        abschreibst).</p>
                </div>
            </section>

            <section id="hilfe-spielerdetails" class="mb-7 scroll-mt-4">
                <p class="ri-section-label">Spielerdetails</p>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Warum stimmt meine
                        Position unter „Aktuelle Position" nicht mit der Rangliste überein?</p>
                    <p class="ri-faq-a">Die Position in der Jahrgangsrangliste betrachtet immer nur
                        einen Doppeljahrgang. Ein jüngerer
                        Spieler kann durchaus mehr Punkte erspielt haben. Da die Positionen über
                        alle Kinder und Jugendlichen
                        errechnet werden, kann die Gesamtposition in der Altersklasse „schlechter"
                        sein als auf der
                        Jahrgangsrangliste.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Was bedeutet „AK"
                        in der Tabelle „Aktuelle Position"?</p>
                    <p class="ri-faq-a">AK steht für <strong>Altersklassenranking</strong>. Neben
                        der allgemeinen Rangliste — die alle jüngeren Jahrgänge einschließt —
                        veröffentlicht der DTB eine offizielle Altersklassenrangliste für genau
                        zwei Geburtsjahrgänge gleichzeitig (den sogenannten Doppeljahrgang,
                        z.&nbsp;B. U14 = Jahrgänge 2010 und 2011). Die AK-Zahl zeigt, auf welchem
                        Platz Du in genau dieser offiziellen Doppeljahrgangsrangliste stehst. Sie
                        erscheint nur in der Zeile des entsprechenden Doppeljahrgangs.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Wie lese ich die
                        Diagramme auf dem Spielerprofil?</p>
                    <p class="ri-faq-a">Auf dem Spielerprofil gibt es vier Diagramme, jeweils als
                        Kurz- (12 Monate) und Langfassung (gesamter Zeitraum):
                        <br><br>
                        <strong>Verlauf Altersklasse</strong> (Liniendiagramm): Zeigt den
                        Ranglistenplatz über die Zeit. Die Y-Achse ist umgekehrt — weiter oben im
                        Diagramm bedeutet ein besserer Rang. Wechselt ein Spieler die Altersklasse,
                        ändert sich auch das Linienlabel (z.&nbsp;B. von U14 auf U16). Im
                        Gesamtverlauf entsteht so eine typische Treppe, die das Aufsteigen durch
                        die Altersklassen zeigt.
                        <br><br>
                        <strong>Verlauf Punkte</strong> (Balkendiagramm): Zeigt die erreichten
                        Punkte pro Quartal. Höhere Balken stehen für mehr Punkte. Sonderwerte
                        wie „PR" (Platzierungsranking), „Einst." (Einsteiger) oder „0,0" werden
                        nicht dargestellt.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Was bedeuten die
                        Aufstiegs-/Abstiegssymbole?</p>
                    <p class="ri-faq-a">Die Symbole zeigen, wie sich Deine Position in der
                        Altersklasse — bezogen auf alle Einträge der
                        Rangliste — im Vergleich zur letzten Veröffentlichung verändert hat. Bei den
                        Punkten ist das absolut.
                        In einer Altersklasse kannst Du Positionen gutmachen, in einer anderen
                        verlieren, weil jeweils alle
                        Spielerinnen und Spieler betrachtet werden.</p>
                </div>
            </section>

            <section id="hilfe-ranglisten" class="mb-7 scroll-mt-4">
                <p class="ri-section-label">Ranglisten</p>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Wie bekomme ich die
                        Jahresabschluss-Rangliste angezeigt?</p>
                    <p class="ri-faq-a">Wähle ein Abschlussquartal und hake die Checkbox
                        „Abschlussrangliste" an — Du erhältst die
                        Rangliste vor dem Altersklassenwechsel.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Was bewirkt die
                        Einstellung „nur Geburtsjahrgang"?</p>
                    <p class="ri-faq-a">Es werden nur Spielerinnen und Spieler angezeigt, die nach
                        Geburtsjahrgang in der Altersklasse
                        sind. Beispiel: „U11 Juniorinnen" und „nur Geburtsjahrgang" zeigt 2020 nur
                        die Spielerinnen des
                        Jahrgangs 2009. Alle weiteren Filter bleiben aktiv.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Was bewirkt „auch
                        Jüngere mit einbeziehen"?</p>
                    <p class="ri-faq-a">Diese Auswahl zeigt nicht nur die Jahrgangsrangliste,
                        sondern bezieht alle jüngeren Spielerinnen
                        und Spieler mit ein — nützlich, wenn Du in einer höheren Altersklasse
                        spielen willst und wissen
                        möchtest, ob es für die Zulassungsliste reicht.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Werden
                        Neueinstufungen nachträglich einberechnet?</p>
                    <p class="ri-faq-a">Bei Fehlern in der offiziellen Berechnung macht der DTB eine
                        separat veröffentlichte
                        Neueinstufung für Turnierveranstalter. Solange der DTB danach keine neue
                        Rangliste veröffentlicht,
                        kann <em>Ranking-Info</em> diese nicht mit einbeziehen.</p>
                </div>
            </section>

            <section id="hilfe-vereine" class="mb-7 scroll-mt-4">
                <p class="ri-section-label">Vereine</p>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Welche Spieler
                        tauchen bei den Vereinen auf?</p>
                    <p class="ri-faq-a">Die Vereinsübersicht zeigt alle Spielerinnen und Spieler,
                        die im aktuellen Quartal auf der
                        Rangliste unter diesem Verein geführt werden. Historische Daten werden hier
                        nicht angezeigt.</p>
                </div>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Warum finde ich
                        einen Verein nicht immer?</p>
                    <p class="ri-faq-a">Hat ein Verein im aktuellen Quartal niemanden auf der
                        Rangliste, taucht er in der Suche nicht
                        auf. Das kann sich von Quartal zu Quartal ändern.</p>
                </div>
            </section>

            <section id="hilfe-verbaende" class="mb-7 scroll-mt-4">
                <p class="ri-section-label">Verbände</p>
                <div class="ri-faq">
                    <p class="ri-faq-q">@template.fragments.icons.help_circle() Was sehe ich in der
                        Verbandsübersicht?</p>
                    <p class="ri-faq-a">Die Verbandsübersicht zeigt für jeden Doppeljahrgang die
                        Anzahl der Spielerinnen und Spieler,
                        die für die jeweiligen Landesverbände auf der Rangliste geführt werden.</p>
                </div>
            </section>

            <section id="hilfe-mobile" class="scroll-mt-4">
                <p class="ri-section-label">Mobile Nutzung</p>
                <div class="ri-note">
                    <em>Ranking-Info</em> funktioniert am besten auf einem normalgroßen Bildschirm
                    und auf den meisten
                    Tablets. Die meisten Ansichten sind auch für Smartphones optimiert — bei großen
                    Datenmengen kann es
                    aber nötig sein, horizontal zu scrollen. An der Mobiloptimierung wird
                    kontinuierlich gearbeitet.
                </div>
            </section>
        </div>
    </div>
`)
