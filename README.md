# ProjetReseau

## Lancement de l'application

Il suffit d'aller dans le dossier du Projet Reseau, ensuite d'aller dans le dossier bindist et ensuite bin. Pour finir, il faut lancer un invite de commandes et lancer run.

![Image](https://imgur.com/gKnP9Im.png)

## Noms de domaines utilisés 

Nous avons donc plusieurs noms de domaine dans notre fichier /etc/hosts qui sont :
  - 127.0.0.1 verti
  - 127.0.0.1 dopetrope
  - 127.0.0.1 paradigmShift

On peut donc naviguer à l'addresse verti/ ou dopetrope/ ou encore paradigmShift/ pour avoir le site internet concerné après avoir lancer le serveur.

![Image](https://i.imgur.com/iwCdZFZ.png)

## Fonctionnalité bonus choisie 

On a choisi comme fonctionnalité bonus de compresser les ressources en gzip.
On peut utiliser la console de développeur (F12) pour vérifier que la taille de transfert est bien inféreure à la taille réelle, et donc que le navigateur a bien décompressé les ressources.

![Image](https://i.imgur.com/HGoSLXUh.jpg)

## Remarques

Pour la protection des ressources, il faut placer un fichier .htpasswd dans le dossier du site en question, en première ligne, il faut mettre le nom de l'utilisateur et en deuxième ligne, le mot de passe en format MD5. Il existe déjà un fichier .htpasswd pour le site Verti, le nom de l'utilisateur est "root" et le mot de passe est "admin". On arrive donc sur une page de connexion.

![Image](https://media.discordapp.net/attachments/781100754206720021/843895320479662080/unknown.png)

Notre approche est naïve et peu sécurisée. Il suffit qu'un cookie avec le nom du site existe pour qu'on se connecte, et ce cookie se créer quand la connexion est établie.

Pour se déconnecter, il suffit de supprimer le cookie manuellement.

![Image](https://media.discordapp.net/attachments/781100754206720021/843895965596254238/unknown.png)



