export class IbanValidator {
  static validate(iban: string): Boolean {
    console.log(iban, this.integerizeIban(iban), this.mod97(this.integerizeIban(iban)), this.mod97(this.integerizeIban(iban)) == 1)
    return this.mod97(this.integerizeIban(iban)) == 1;
  }

  private static integerizeIban(iban: string): string {
    const trimmedIban: string = iban.replace(/\s/ig, '');
    const adjustedIban = trimmedIban.substring(4) + trimmedIban.substring(0, 4);

    return adjustedIban.split('').map(this.integerizeIbanDigit).join('');
  }

  /**
   * Replace letters with a 2 digit number, e.g. A = 10, B = 11, C = 12, ..., Z = 35
   */
  private static integerizeIbanDigit(digit: string): number {
    let integerizedValue = parseInt(digit);
    if (isNaN(integerizedValue)) {
      integerizedValue = digit.charCodeAt(0) - 55
    }
    return integerizedValue;
  }

  private static mod97(divident: string): number {
    return this.modulo(divident, 97);
  }

  /**
   * See http://stackoverflow.com/questions/929910/modulo-in-javascript-large-number/16019504#16019504
   * See https://en.wikipedia.org/wiki/International_Bank_Account_Number#Modulo_operation_on_IBAN
   */
  private static modulo(divident: string, divisor: number): number {
    const nMaxLength = 9;

    while (divident.length > nMaxLength) {
      const n = divident.substring(0, nMaxLength);
      divident = (parseInt(n) % divisor) + divident.substring(nMaxLength);
    }

    return parseInt(divident) % divisor;
  }
}