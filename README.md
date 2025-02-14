# Pipeline

## Descrição do Projeto


### Autores
- Maria Eduarda Matias de Brites Simões
- Pedro Henrique José Carvalho Lopes

## Introdução

A arquitetura MIPS é uma arquitetura de computadores do tipo RISC (Reduced Instruction Set Computing) utilizada em diversos eletrônicos, incluindo os consoles PlayStation e PlayStation 2. Sua simplicidade e facilidade de entendimento tornam-na um tópico comum nos cursos de Computação.

## Estrutura do Projeto

### Arquivos (Se encontram em src/assets)

- **program.txt:** Arquivo de entrada contendo as instruções MIPS.
- **programOut.txt:** Arquivo de saída que será gerado com a representação binária das instruções.

### Classes

- **Main:** Esta classe instancia um objeto da classe `ReadAndWriteTxt` e chama seu método `txtReader`, passando o caminho do arquivo que será lido.


- **Instructions:** Responsável por direcionar as instruções, esta classe verifica o tipo de instrução baseado no 'mnemonic' e instancia um objeto do tipo correspondente à instrução. Por exemplo, para a instrução `lw` (um mnemonic do tipo I), o método `setInstruction (da própria classe)` determina o tipo da instrução e, sendo do tipo I, chama o método `setInstruction` da classe `TypeI`.


- **ReadAndWriteTxt:** A classe `ReadAndWriteTxt` é responsável por ler cada linha de instrução do arquivo `program.txt`. Ela possui um método para dividir a linha em partes, usando espaços como delimitadores, e as armazenam em um array. Após dividir a linha, a classe instancia um objeto `Instructions` e passa o array das instruções divididas em sua inicialização.


- **Registers:** A classe `Registers` possui um método que adiciona todos os registradores e suas representações binárias em um `HashMap`. Ela também possui um método `getRegister` que recebe o nome do registrador (a chave do `HashMap`) e retorna sua representação binária (o valor do `HashMap`).


- **Tools:** Esta classe fornece ferramentas utilizadas no código, como a conversão de decimal para binário e a adição de zeros à esquerda.


- **TypeI:** A classe `TypeI` contém métodos necessários para receber uma instrução do tipo I como parâmetro em seu método `setInstruction` e retornar a representação binária dessa instrução no método `getBinaryInstruction`. Ela também possui um `HashMap` que contém o nome da instrução (chave do `HashMap`) e sua representação binária (valor do `HashMap`).


- **TypeJ:** Semelhante à classe `TypeI`, a classe `TypeJ` possui todos os métodos para transformar uma instrução do tipo J em sua representação binária.


- **TypeR:** Como as classes `TypeI` e `TypeJ`, a `TypeR` também possui métodos para transformar instruções do tipo R em suas representações binárias. A diferença entre essas classes está nas especificidades de cada tipo de instrução. Por exemplo, a organização dos bits em cada instrução MIPS:
    - Tipo R: `opcode(6)|rs(5)|rt(5)|rd(5)|shamt(5)|function(6)`
    - Tipo I: `opcode(6)|rs(5)|rt(5)|immediate(16)`
    - Tipo J: `opcode(6)|address(26)`


## Instruções para Execução

   - Coloque as instruções MIPS no arquivo `program.txt`.
   - Execute o programa.
   - O programa lerá as instruções do arquivo `program.txt` e escreverá no arquivo `programOut.txt` com a representação binária das instruções.


