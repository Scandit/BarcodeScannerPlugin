/*
  Declaration files are how the Typescript compiler knows about the type information(or shape) of an object.
  They're what make intellisense work and make Typescript know all about your code.

  A wildcard module is declared below to allow third party libraries to be used in an app even if they don't
  provide their own type declarations.

  To learn more about using third party libraries in an Ionic app, check out the docs here:
  http://ionicframework.com/docs/v2/resources/third-party-libs/

  For more info on type definition files, check out the Typescript docs here:
  https://www.typescriptlang.org/docs/handbook/declaration-files/introduction.html
*/
declare module '*';

type BarcodePicker = any;
type ScanSettings = any;
type UiSettings = {
  viewfinder: {
    style: number, // enum
    portrait: {
      width: number,
      height: number,
    },
    landscape: {
      width: number,
      height: number,
    },
  },
  searchBar: boolean,
  feedback: {
    beep: boolean,
    vibrate: boolean,
  },
  torch: {
    enabled: boolean,
    offset: {
      left: number,
      top: number,
    }
  },
  cameraSwitch: {
    visibility: number, // enum
    offset: {
      right: number,
      top: number,
    },
  },
};

type Constraint = number | string;

type Constraints = {
  topMargin?: Constraint,
  rightMargin?: Constraint,
  bottomMargin?: Constraint,
  leftMargin?: Constraint,
  width?: Constraint,
  height?: Constraint,
}

type Margin = number;

type Margins = {
  top: Margin,
  right: Margin,
  bottom: Margin,
  left: Margin,
}

declare let Scandit;
