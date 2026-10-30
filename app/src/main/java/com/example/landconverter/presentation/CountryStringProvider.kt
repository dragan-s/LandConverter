package com.example.landconverter.presentation

import com.example.landconverter.data.model.BalkanCountry

data class AppUiStrings(
    val appTitle: String,
    val amountLabel: String,
    val fromUnitLabel: String,
    val toUnitLabel: String,
    val countryLabel: String,
    val resultTitle: String,
    val manageUnitsButton: String,
    val errorInvalidNumber: String
)

data class ManageUnitsUiStrings(
    val title: String,
    val saveButton: String,
    val countryLabel: String,
    val addSectionTitle: (countryName: String) -> String,
    val unitNameLabel: String,
    val unitValueLabel: String,
    val addUnitButton: String,
    val existingUnitsHeader: String,
    val emptyUnitsMessage: String,
    val unitNameFieldLabel: String,
    val deleteUnitContentDescription: String,
    val unsavedChangesTitle: String,
    val unsavedChangesText: String,
    val discardButton: String,
    val continueEditingButton: String,
    val confirmDeleteTitle: String,
    val confirmDeleteText: (unitName: String) -> String,
    val deleteConfirmButton: String,
    val cancelButton: String,
    val errNameRequired: String,
    val errValueInvalid: String,
    val errDuplicateName: String,
    val errDuplicateNameInState: String,
    val errInvalidData: String,
    val errSaveFailed: String
)

object CountryStringProvider {
    fun getStrings(country: BalkanCountry): AppUiStrings {
        return when (country) {
            is BalkanCountry.BosniaAndHerzegovina -> AppUiStrings(
                appTitle = "Konvertor površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                manageUnitsButton = "Upravljaj merama",
                errorInvalidNumber = "Unesite ispravan pozitivan broj"
            )
            is BalkanCountry.Serbia -> AppUiStrings(
                appTitle = "Pretvarač površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                manageUnitsButton = "Upravljaj merama",
                errorInvalidNumber = "Unesite ispravan pozitivan broj"
            )
            is BalkanCountry.Montenegro -> AppUiStrings(
                appTitle = "Konvertor površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                manageUnitsButton = "Upravljaj merama",
                errorInvalidNumber = "Unesite važeći pozitivan broj"
            )
            is BalkanCountry.NorthMacedonia -> AppUiStrings(
                appTitle = "Конвертор на површина на земјиште",
                amountLabel = "Количина",
                fromUnitLabel = "Од единица",
                toUnitLabel = "До единица",
                countryLabel = "Држава",
                resultTitle = "Резултат",
                manageUnitsButton = "Управувај со мерки",
                errorInvalidNumber = "Внесете валиден позитивен број"
            )
            is BalkanCountry.Croatia -> AppUiStrings(
                appTitle = "Pretvornik površine zemljišta",
                amountLabel = "Količina",
                fromUnitLabel = "Iz jedinice",
                toUnitLabel = "U jedinicu",
                countryLabel = "Država",
                resultTitle = "Rezultat",
                manageUnitsButton = "Upravljaj mjerama",
                errorInvalidNumber = "Unesite ispravan pozitivan broj"
            )
        }
    }

    fun getManageUnitsStrings(country: BalkanCountry): ManageUnitsUiStrings {
        return when (country) {
            is BalkanCountry.BosniaAndHerzegovina -> ManageUnitsUiStrings(
                title = "Upravljaj merama",
                saveButton = "Sačuvaj",
                countryLabel = "Država / sistem",
                addSectionTitle = { countryName -> "Dodaj novu meru za $countryName" },
                unitNameLabel = "Naziv mere (npr. Motika)",
                unitValueLabel = "Vrednost u m²",
                addUnitButton = "Dodaj meru",
                existingUnitsHeader = "Postojeće mere",
                emptyUnitsMessage = "Nema prilagođenih mera za ovu državu.",
                unitNameFieldLabel = "Naziv",
                deleteUnitContentDescription = "Obriši meru",
                unsavedChangesTitle = "Nesačuvane izmene",
                unsavedChangesText = "Imate izmene koje nisu sačuvane. Da li želite da ih odbacite?",
                discardButton = "Odbaci",
                continueEditingButton = "Nastavi uređivanje",
                confirmDeleteTitle = "Potvrda brisanja",
                confirmDeleteText = { unitName -> "Da li ste sigurni da želite da obrišete meru \"$unitName\"? Izmena će biti sačuvana tek kada pritisnete \"Sačuvaj\"." },
                deleteConfirmButton = "Obriši",
                cancelButton = "Otkaži",
                errNameRequired = "Naziv mere je obavezan.",
                errValueInvalid = "Unesite ispravnu pozitivnu vrednost u m².",
                errDuplicateName = "Mera sa ovim nazivom već postoji u izabranoj državi.",
                errDuplicateNameInState = "Postoje izmene sa istim nazivom mere u izabranoj državi.",
                errInvalidData = "Neispravni podaci u izmenama mera.",
                errSaveFailed = "Greška pri čuvanju izmena. Pokušajte ponovo."
            )
            is BalkanCountry.Serbia -> ManageUnitsUiStrings(
                title = "Upravljaj merama",
                saveButton = "Sačuvaj",
                countryLabel = "Država / sistem",
                addSectionTitle = { countryName -> "Dodaj novu meru za $countryName" },
                unitNameLabel = "Naziv mere (npr. Motika)",
                unitValueLabel = "Vrednost u m²",
                addUnitButton = "Dodaj meru",
                existingUnitsHeader = "Postojeće mere",
                emptyUnitsMessage = "Nema prilagođenih mera za ovu državu.",
                unitNameFieldLabel = "Naziv",
                deleteUnitContentDescription = "Obriši meru",
                unsavedChangesTitle = "Nesačuvane izmene",
                unsavedChangesText = "Imate izmene koje nisu sačuvane. Da li želite da ih odbacite?",
                discardButton = "Odbaci",
                continueEditingButton = "Nastavi uređivanje",
                confirmDeleteTitle = "Potvrda brisanja",
                confirmDeleteText = { unitName -> "Da li ste sigurni da želite da obrišete meru \"$unitName\"? Izmena će biti sačuvana tek kada pritisnete \"Sačuvaj\"." },
                deleteConfirmButton = "Obriši",
                cancelButton = "Otkaži",
                errNameRequired = "Naziv mere je obavezan.",
                errValueInvalid = "Unesite ispravnu pozitivnu vrednost u m².",
                errDuplicateName = "Mera sa ovim nazivom već postoji u izabranoj državi.",
                errDuplicateNameInState = "Postoje izmene sa istim nazivom mere u izabranoj državi.",
                errInvalidData = "Neispravni podaci u izmenama mera.",
                errSaveFailed = "Greška pri čuvanju izmena. Pokušajte ponovo."
            )
            is BalkanCountry.Montenegro -> ManageUnitsUiStrings(
                title = "Upravljaj merama",
                saveButton = "Sačuvaj",
                countryLabel = "Država / sistem",
                addSectionTitle = { countryName -> "Dodaj novu meru za $countryName" },
                unitNameLabel = "Naziv mere (npr. Motika)",
                unitValueLabel = "Vrednost u m²",
                addUnitButton = "Dodaj meru",
                existingUnitsHeader = "Postojeće mere",
                emptyUnitsMessage = "Nema prilagođenih mera za ovu državu.",
                unitNameFieldLabel = "Naziv",
                deleteUnitContentDescription = "Obriši meru",
                unsavedChangesTitle = "Nesačuvane izmene",
                unsavedChangesText = "Imate izmene koje nisu sačuvane. Da li želite da ih odbacite?",
                discardButton = "Odbaci",
                continueEditingButton = "Nastavi uređivanje",
                confirmDeleteTitle = "Potvrda brisanja",
                confirmDeleteText = { unitName -> "Da li ste sigurni da želite da obrišete meru \"$unitName\"? Izmena će biti sačuvana tek kada pritisnete \"Sačuvaj\"." },
                deleteConfirmButton = "Obriši",
                cancelButton = "Otkaži",
                errNameRequired = "Naziv mere je obavezan.",
                errValueInvalid = "Unesite ispravnu pozitivnu vrednost u m².",
                errDuplicateName = "Mera sa ovim nazivom već postoji u izabranoj državi.",
                errDuplicateNameInState = "Postoje izmene sa istim nazivom mere u izabranoj državi.",
                errInvalidData = "Neispravni podaci u izmenama mera.",
                errSaveFailed = "Greška pri čuvanju izmena. Pokušajte ponovo."
            )
            is BalkanCountry.NorthMacedonia -> ManageUnitsUiStrings(
                title = "Управувај со мерки",
                saveButton = "Зачувај",
                countryLabel = "Држава / систем",
                addSectionTitle = { countryName -> "Додај нова мерка за $countryName" },
                unitNameLabel = "Назив на мерка (нпр. Мотика)",
                unitValueLabel = "Вредност во m²",
                addUnitButton = "Додај мерка",
                existingUnitsHeader = "Постоечки мерки",
                emptyUnitsMessage = "Нема прилагодени мерки за оваа држава.",
                unitNameFieldLabel = "Назив",
                deleteUnitContentDescription = "Избриши мерка",
                unsavedChangesTitle = "Незачувани измени",
                unsavedChangesText = "Имате измени кои не се зачувани. Дали сакате да ги отфрлите?",
                discardButton = "Отфрли",
                continueEditingButton = "Продолжи со уредување",
                confirmDeleteTitle = "Потврда за бришење",
                confirmDeleteText = { unitName -> "Дали сте сигурни дека сакате да ја избришете мерката \"$unitName\"? Измената ќе биде зачувана дури кога ќе притиснете \"Зачувај\"." },
                deleteConfirmButton = "Избриши",
                cancelButton = "Откажи",
                errNameRequired = "Името на мерката е задолжително.",
                errValueInvalid = "Внесете валидна позитивна вредност во m².",
                errDuplicateName = "Мерка со ова име веќе постои во избраната држава.",
                errDuplicateNameInState = "Постојат измени со исто име на мерка во избраната држава.",
                errInvalidData = "Невалидни податоци во измените на мерките.",
                errSaveFailed = "Грешка при зачувување на измените. Обидете се повторно."
            )
            is BalkanCountry.Croatia -> ManageUnitsUiStrings(
                title = "Upravljaj mjerama",
                saveButton = "Spremi",
                countryLabel = "Država / sustav",
                addSectionTitle = { countryName -> "Dodaj novu mjeru za $countryName" },
                unitNameLabel = "Naziv mjere (npr. Motika)",
                unitValueLabel = "Vrijednost u m²",
                addUnitButton = "Dodaj mjeru",
                existingUnitsHeader = "Postojeće mjere",
                emptyUnitsMessage = "Nema prilagođenih mjera za ovu državu.",
                unitNameFieldLabel = "Naziv",
                deleteUnitContentDescription = "Obriši mjeru",
                unsavedChangesTitle = "Nespremljene izmjene",
                unsavedChangesText = "Imate izmjene koje nisu spremljene. Želite li ih odbaciti?",
                discardButton = "Odbaci",
                continueEditingButton = "Nastavi uređivanje",
                confirmDeleteTitle = "Potvrda brisanja",
                confirmDeleteText = { unitName -> "Jeste li sigurni da želite obrisati mjeru \"$unitName\"? Izmjena će biti spremljena tek kada pritisnete \"Spremi\"." },
                deleteConfirmButton = "Obriši",
                cancelButton = "Odustani",
                errNameRequired = "Naziv mjere je obavezan.",
                errValueInvalid = "Unesite ispravnu pozitivnu vrijednost u m².",
                errDuplicateName = "Mjera s ovim nazivom već postoji u odabranoj državi.",
                errDuplicateNameInState = "Postoje izmjene s istim nazivom mjere u odabranoj državi.",
                errInvalidData = "Neispravni podaci u izmjenama mjera.",
                errSaveFailed = "Pogreška pri spremanju izmjena. Pokušajte ponovo."
            )
        }
    }
}
