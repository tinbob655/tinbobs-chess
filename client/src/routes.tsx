import React from 'react';
import {Route, Routes} from 'react-router';


//import all pages
import Home from './components/pages/home/home';

export default function AllRoutes():React.ReactElement {

    return (
        <Routes>
            {getRoutes()}
        </Routes>
    );
}

function getRoutes():React.ReactElement[] {

    let res:React.ReactElement[] = [];
    const pages:[string, React.ReactElement][] = [
        ['', <Home/>],
    ];

    pages.forEach((page) => {
        res.push(
            <Route path={page[0]} element={page[1]} />
        );
    });
    
    return res;
}