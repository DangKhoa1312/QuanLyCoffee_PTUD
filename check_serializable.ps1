Get-ChildItem -Path src/entity, src/dto -Filter *.java | Select-String -Pattern implements Serializable
