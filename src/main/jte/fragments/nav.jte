<header>
    <nav class="bg-brand border-b-2 border-accent/30" role="navigation" aria-label="Hauptnavigation">
        <div class="max-w-screen-xl mx-auto flex flex-wrap items-center justify-between px-4">
            <a href="/" class="flex items-center py-2.5">
                <img src="/images/ri-logo-nav.svg" class="h-7 w-auto"
                     alt="Ranking-Info"
                     onerror="this.onerror=null;this.src='/images/ri-logo.png'">
            </a>
            <button type="button" id="nav-burger" class="md:hidden text-white p-2 rounded hover:bg-white/20"
                    aria-label="Menü öffnen" aria-expanded="false" aria-controls="nav-menu">
                <span class="block w-5 h-0.5 bg-white mb-1"></span>
                <span class="block w-5 h-0.5 bg-white mb-1"></span>
                <span class="block w-5 h-0.5 bg-white"></span>
            </button>
            <div id="nav-menu" class="ri-nav hidden w-full md:flex md:w-auto md:items-center md:gap-1 pb-2 md:pb-0">
                <a href="/players" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Spieler</a>
                <a href="/listings" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Listen</a>
                <a href="/clubs" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Vereine</a>
                <a href="/federations" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Verbände</a>
                <a href="/status" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Status</a>
                <a href="/help" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Hilfe</a>
                <a href="/privacy" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Datenschutz</a>
                <a href="/about" class="text-white/70 hover:text-white px-3 py-2 text-sm block md:inline-block transition-colors">Über</a>
            </div>
        </div>
    </nav>
    <script>
        (function () {
            const burger = document.getElementById('nav-burger');
            const menu = document.getElementById('nav-menu');
            burger?.addEventListener('click', () => {
                const isHidden = menu.classList.toggle('hidden');
                burger.setAttribute('aria-expanded', isHidden ? 'false' : 'true');
            });
            // Mark the active top-level navigation item
            const path = window.location.pathname;
            menu?.querySelectorAll('a').forEach((a) => {
                const href = a.getAttribute('href');
                if (!href || href === '/') return;
                if (path === href || path.indexOf(href + '/') === 0) {
                    a.classList.add('is-active');
                }
            });
        })();
    </script>
</header>
