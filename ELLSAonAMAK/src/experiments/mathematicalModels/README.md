CHANGEMENTS EFFECTUÉS POUR AMÉLIORER LE FACTEUR REPRODUCTIBILITÉ:

-Regroupement de tous les paramètres de l'expérimentation dans la classe java PARAMS.
Elle est décomposée en plusieurs sections dépendant de la partie de l'expérience que l'on veut modifier.

-Création d'un fichier "text.xml" qui va contenir toutes les valeurs pour chaque paramètre.

-Création de la classe LaunchExampleXPWithXML.java qui va paramétrer l'expérience en parcourant le fichier
.xml. À noter que certains paramètres sont définis par rapport à d'autres.
Pour récupérer les attributs de chaque namespaces xml on passe par une NodeList et ensuite on vient parser si besoin
la valeur des attributs récupérer par la lecture.

PROTOCOL EXPERIMENTAL

- Définir les valeurs des paramètres au travers d'un fichier .xml placer dans /resources et suivant le model de test.xml
- Passer le fichier .xml en paramètre de la classe LaunchExampleXPWithXML.java
- Lancer le launcher.
