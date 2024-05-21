# Detyra e tretë nga lënda "Siguri e të Dhënave"

Ky projekt është një console aplikacion i cili shërben për komunikimin e sigurt të çelësit simetrik duke përdorur enkriptimin RSA. Komunikimi ndodhë mes një serveri dhe një klienti. Serveri gjeneron një çelës simetrik, e kodon atë me çelësin publik RSA të klientit dhe ia dërgon klientit. Klienti deshifron çelësin simetrik me çelësin e tij privat RSA dhe e përdor atë për komunikim të sigurt.

## Parakushtet

- Java Development Kit (JDK) të instaluar në sistemin tuaj
- Një IDE të përshtatshme të instaluar në kompjuterin tuaj

## Detajet e egzekutimit

1. **Clone the repository**:

   ```bash
   git clone https://github.com/lumii06/GR10_DataSecurity_Det3.git
2. **Navigoni për në direktoriumin e projektit**:

```bash
   cd GR10_DataSecurity_Det3
```
3. **Kompajlloni java files**:

```bash
   javac RSAServer.java RSAClient.java
```
