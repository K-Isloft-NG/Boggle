const minuteur = document.querySelector(".minuteur");

if (minuteur) {
    const affichage = minuteur.querySelector("span");
    const fin = Number(minuteur.dataset.fin);
    const instant = Number(minuteur.dataset.instant);
    const echeance = Date.now() + (fin - instant) * 1000;
    const intervalle = setInterval(actualiser, 1000);

    function actualiser() {
        const secondes = Math.max(0, Math.ceil((echeance - Date.now()) / 1000));
        const minutes = Math.floor(secondes / 60);
        affichage.textContent = minutes + ":" + String(secondes % 60).padStart(2, "0");

        if (secondes === 0) {
            clearInterval(intervalle);
            window.location.replace("/?page=partie");
        }
    }

    actualiser();
}
