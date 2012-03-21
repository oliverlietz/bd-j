# id

id tool converts an `id.bdmv` file to an xml format and back. `id.bdmv` file includes the **disc ID** and **org ID** for a given BD image, and required to be present under the `CERTIFICATE` dir.

`56789abc` is used as organization ID for test discs.

## usage

    java -jar id.jar id.xml id.bdmv
    java -jar id.jar id.bdmv id.xml
