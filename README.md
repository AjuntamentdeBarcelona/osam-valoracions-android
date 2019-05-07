# osam-valoracions-android
[![](https://jitpack.io/v/AjuntamentdeBarcelona/osam-valoracions-android.svg)](https://jitpack.io/#AjuntamentdeBarcelona/osam-valoracions-android)
# README

## Objectiu

Aquest document és una guia de com s’utilitza la llibreria ‘RateMeMaybe’. 

## Descripció de la funcionalitat

La seva funcionalitat és mostrar periòdicament una popup que convida a l’usuari a deixar un comentari a la llista de comentaris de l’app al market place corresponent (Google Play, iTunes o Windows Market). 
La popup té tres botons:
-	Positiu (“VALORAR ARA”): El sistema obre la web de l’app client en el market, i l’usuari només haurà de fer ‘new review’ i deixar el seu comentari i valoració sobre l’app.
-	Negatiu (“NO, GRÀCIES”).  El popup es tanca i no tornarà a aparèixer.
-	Neutre (“MÉS TARD”). El popup es tanca i tornarà a aparèixer en un futur.

##Operativa

Funcionament:
- L’app compta cada vegada que s’obre. 
- L’app espera a que passin un nº de dies determinats (p.ex. 90) des de l’últim cop que ha mostrat la pop up (per tal de l’usuari no la consideri intrusiva o abusiva).
- Un cop passats aquests dies, quan el comptador superi un valor determinat (p.ex. 20), mostra la pop up i el comptador es reinicia independentment de la resposta de l’usuari. 
- La operativa no es veu modificada si hi ha un canvi de versió (és a dir, es mantenen els valors de comptatge de dies i de nº de apertures).
- El text que es mostra al popup és configurable des del servei

Paràmetres: L’app farà ús de 3 paràmetres:
- Tmin: Nº de dies mínims que han de passar per que l’app mostri la popup. 
- Num_apert: nº de vegades que ha cal obrir l’app per tal que es mostri la popup (si s’ha superat Tmin).
Si Num_apert = 0 llavors no es mostra mai al popup (popup desactivada).
- Messages: llista d’elements format cada un pel tipus de llenguatge i contingut del missatge. Aquest missatge serà el que es mostrarà al popup
	- Language: tipus d’idioma
	- Content: contingut del missatge en l’idioma especificat

Gestió dels paràmetres: Hi ha 2 opcions per gestionar (editar) els paràmetres:
- Posar-los dins de l’app. Això té l’inconvenient de que quan es vulgui desactivar aquesta funcionalitat caldrà crear una nova versió i publicar-la. 
- Fer una crida a un web service des de l’app. Aquest servei ha d’enviar un JSON amb aquest format:

```
{
	"tmin": int,
	"num_apert": int,
	"messages": [
	{
		"languaje": String,
		"content": String
	}
	]
}
```

Si el JSON es canvia i l’app esta configurada perquè es descarregui la configuració d’aquest, la configuració s’actualitzarà i posteriorment es comprovarà si es compleixen les noves condicions per decidir si es mostra o no el popup.

## Llibreria RateMeMaybe Android

L’app s’ha de comunicar amb la llibreria a través de la classe RateMeMaybe. Li podem configurar els paràmetres tmin, num_apert i text directament per codi o indicant la URL del servei que els informa.

void run()
Comprova els paràmetres i si cal mostra la popup. Normalment es crida des de la Activity “launcher“ de l’app.

void forceShow()
Mostra la popup directament

static void resetData(FragmentActivity activity)
Reseteja els comptadors de dies i apertures de l’app.

void setIcon(int customIcon)
Id de la icona que es mostra en la popup. Si es 0, es mostrarà la mateixa que té la app.

void setButtonsTextColor(int mButtonsTextColor)
void setTitleColor(int mTitleColor)

void setMessageColor(int mMessageColor)
void setBackgroundColor(int mBackgroundColor)
Colors dels elements de la popup.

void setTmin(int tmim)
void setNumApert(int numApert)
void setText(String text)
Valors dels paràmetres de la popup (si els setegem per codi).

void setServiceUrl(String serviceUrl)
URL de la qual obtenir els valors dels paràmetres.

void setLanguage(String language)
Idioma amb el que volem el text del popup.

void setHandleCancelAsNeutral(Boolean handleCancelAsNeutral)
Si val “true” (valor per defecte) cancelar la popup (p.ex. prement el botó back) equival a clicar el botó neutre. Si val “false” equival a clicar el botó negatiu.

void setAdditionalListener(OnRMMUserChoiceListener listener)
Afegeix un listener (normalment serà la Activity o Fragment des del que es crida a la llibrería) a les accions dels botons de la popup.

void setRunWithoutPlayStore(Boolean runWithoutPlayStore)
Si val “true” la popup pot aparéixer encara que el market no estigui disponible en el dispositiu. Per defecte val “false”

L’APK conté un sample (mòdul “app”) que mostra com utilitzar la llibreria:

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    askForRating();
}

private void askForRating() {
        //RateMeMaybe.resetData(this);
        RateMeMaybe rmm = new RateMeMaybe(this);
        rmm.setButtonsTextColor(Color.YELLOW);
        rmm.setTitleColor(Color.YELLOW);
        rmm.setMessageColor(Color.LTGRAY);
        rmm.setBackgroundColor(Color.BLUE);
        rmm.setIcon(R.drawable.hellokitty_64);
        rmm.setAdditionalListener(new RateMeMaybe.OnRMMUserChoiceListener() {
            @Override
            public void handlePositive() {
                launchHomeActivity();
            }
            @Override
                        public void handleNeutral() {
                launchHomeActivity();
            }
            @Override
            public void handleNegative() {
                launchHomeActivity();
            }
        });        
        //rmm.setHandleCancelAsNeutral(false);
        //rmm.setRunWithoutPlayStore(true);

        /* we can set popup params directly from code */
        Map<String, String> messages = new HashMap<String, String>();
        messages.put(“ca”, “Missatge”);
        messages.put(“es”, “Mensaje”);
        messages.put(“en”, “Message”);
        rmm.setTmin(1);
        rmm.setNumApert(3);
        rmm.setMessages(messages);
        /* or specifying an URL to obtain them in JSON format */
        //rmm.setServiceUrl(SERVICE_URL);
        //rmm.setLanguage(Locale.getDefault().getLanguage())

        //rmm.forceShow();
        rmm.run();
    }

```
Com en totes les aplicacions de la OSAM, o la gran majoria, s’utilitza la SplashScreen, es recomanable utilitzar un ReteMeMaybe.OnRMMUserChoiceListener per capturar els  events del popup que seleccioni l’usuari per poder executar el següent mètode que es vulgui. Podem veure un exemple en el codi que hi ha més amunt.

## How to use it?
- Add these dependencies to your project:
```
compile 'com.github.AjuntamentdeBarcelona:osam-valoracions-android:1.4.0'
```
- Add this to your root build.gradle
```
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```
